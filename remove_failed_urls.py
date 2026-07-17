#!/usr/bin/env python3
"""从失败列表中删除媒体 URL，同时保持 pics_videos.log 的原始 JSON 排版。

脚本只删除 failed.list 中列出的完整 JSON 字符串项；不会重新序列化整份 JSON，
因此不会改变无关字段、缩进、换行和键顺序。写入前会验证 JSON；写入时使用同目录
临时文件原子替换目标文件，且不会创建备份。
"""

from __future__ import annotations

import argparse
import json
import os
from pathlib import Path
import sys
import tempfile


def read_text_preserving_utf8_bom(path: Path) -> tuple[str, str]:
    """读取文本，并返回用于保持 UTF-8 BOM 状态的解码编码名称。

    日志文件可能带 UTF-8 BOM，也可能不带。这里先读取字节判断 BOM，后续写入时
    使用相同编码，避免因脚本运行而改变文件开头的字节或造成中文显示异常。
    """
    # 直接读取原始字节，才能准确识别文件最前面的 UTF-8 BOM。
    raw = path.read_bytes()
    # utf-8-sig 解码会自动跳过 BOM，并会在编码写回时重新补上 BOM。
    encoding = "utf-8-sig" if raw.startswith(b"\xef\xbb\xbf") else "utf-8"
    return raw.decode(encoding), encoding


def remove_json_string_item(text: str, url: str) -> tuple[str, bool, bool]:
    """删除一个完整 JSON 字符串项及其相邻逗号。

    先精确匹配 failed.list 中的完整 URL。若微博视频 URL 的 Expires、ssig 等
    临时查询参数已更新，则退回到稳定的“协议、主机、路径”部分匹配；但仅在该
    路径只命中一个 JSON 字符串时才删除，避免删除到相同视频的多个位置。

    返回值依次为：处理后的文本、是否成功删除、是否使用了稳定路径匹配。
    """
    # JSON 中 URL 是被双引号包裹的字符串，必须连同引号精确定位。
    item = f'"{url}"'
    start = text.find(item)
    used_stable_path = False
    if start < 0:
        # 完整 URL 不存在时，去掉 ? 之后的易过期签名参数，保留稳定视频地址。
        stable_url = url.split("?", maxsplit=1)[0]
        marker = f'"{stable_url}'
        candidates: list[tuple[int, int]] = []
        search_from = 0
        # 逐个寻找稳定地址，收集完整 JSON 字符串的起止位置。
        while (candidate_start := text.find(marker, search_from)) >= 0:
            path_end = candidate_start + len(marker)
            # 路径后只能是查询串开头或字符串闭合引号，防止仅匹配到更长文件名的前缀。
            if path_end < len(text) and text[path_end] in {'?', '"'}:
                candidate_end = text.find('"', path_end)
                if candidate_end >= 0:
                    candidates.append((candidate_start, candidate_end + 1))
            search_from = candidate_start + len(marker)
        # 未找到或匹配多个位置都不安全：交由调用方输出警告并跳过。
        if len(candidates) != 1:
            return text, False, False
        # 只使用唯一候选，保证不会因过期签名变化而误删多个 URL。
        start, end = candidates[0]
        used_stable_path = True
    else:
        # 完整 URL 命中时，结束位置就是带双引号字符串的末尾。
        end = start + len(item)

    # 向后跳过空格、制表符和换行，兼容单行与格式化 JSON。
    after = end
    while after < len(text) and text[after].isspace():
        after += 1
    if after < len(text) and text[after] == ",":
        # 首项或中间项：删除 URL、其后的空白和后置逗号，避免遗留双逗号。
        return text[:start] + text[after + 1 :], True, used_stable_path

    # 未找到后置逗号时，反向跳过空白，检查该项前面的逗号。
    before = start - 1
    while before >= 0 and text[before].isspace():
        before -= 1
    if before >= 0 and text[before] == ",":
        # 末项：删除前置逗号、两者间空白和 URL，保持数组的逗号规则正确。
        return text[:before] + text[end:], True, used_stable_path

    # 数组中仅剩该项时两侧都没有逗号，只删除字符串本身即可。
    return text[:start] + text[end:], True, used_stable_path


def url_is_present(text: str, url: str) -> bool:
    """检查完整 URL 或稳定视频路径是否仍然留在日志中。"""
    # 删除后同时检查完整 URL 和稳定路径，防止签名已更新的 URL 被漏检。
    return f'"{url}"' in text or f'"{url.split("?", maxsplit=1)[0]}' in text


def main() -> int:
    """解析参数、执行精确删除、验证 JSON，并在非预演模式下原子写入。"""
    # 命令行参数只接收一个日志目录；该目录必须包含 failed.list 与 pics_videos.log。
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path, help="Directory containing failed.list and pics_videos.log")
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Validate and report the planned changes without writing pics_videos.log",
    )
    args = parser.parse_args()

    # 解析为绝对路径，便于错误信息和临时文件位置保持明确。
    directory = args.directory.resolve()
    failed_path = directory / "failed.list"
    log_path = directory / "pics_videos.log"
    for path in (failed_path, log_path):
        if not path.is_file():
            parser.error(f"File not found: {path}")

    # 忽略空行；failed.list 使用 utf-8-sig 读取以兼容带 BOM 的历史文件。
    urls = [line.strip() for line in failed_path.read_text(encoding="utf-8-sig").splitlines() if line.strip()]
    if len(urls) != len(set(urls)):
        # 同一 URL 重复出现会让“是否仍残留”的判断失真，因此要求用户先去重。
        parser.error("failed.list contains duplicate URLs; deduplicate it before running the script")

    # 读取原始日志全文；之后只在内存中进行字符串级别的最小替换。
    original, encoding = read_text_preserving_utf8_bom(log_path)
    updated = original
    missing: list[str] = []
    stable_path_matches = 0
    for url in urls:
        # 每次只删除一个完整 JSON 项，不重排 JSON 的其它内容。
        updated, removed, used_stable_path = remove_json_string_item(updated, url)
        if not removed:
            missing.append(url)
        elif used_stable_path:
            stable_path_matches += 1

    if missing:
        # 未找到不是致命错误：记录警告后继续处理其他 URL。
        print(
            f"WARNING: {len(missing)} URL(s) were not found or were not uniquely identifiable; skipped.",
            file=sys.stderr,
        )
        for url in missing:
            print(f"WARNING: skipped {url}", file=sys.stderr)

    # 在触碰原文件前解析完整 JSON，确保逗号处理没有破坏语法。
    json.loads(updated)
    # 再检查每条 URL 的完整形式和稳定形式，确保已删除的项目没有残留。
    remaining = [url for url in urls if url_is_present(updated, url)]
    if remaining:
        raise RuntimeError(f"{len(remaining)} URL(s) still remain after replacement")

    if args.dry_run:
        # 预演只执行所有匹配与 JSON 验证，不写入任何文件。
        print(
            f"Dry run successful: {len(urls) - len(missing)} URL(s) would be removed "
            f"({stable_path_matches} by stable path); JSON is valid."
        )
        return 0

    # 在目标文件同目录创建临时文件，确保 os.replace 可在同一文件系统内原子执行。
    with tempfile.NamedTemporaryFile("w", encoding=encoding, newline="", dir=directory, delete=False) as temp:
        temp.write(updated)
        temp_path = Path(temp.name)
    try:
        # 原子替换可避免写入中断留下半截日志；该操作不会额外创建备份文件。
        os.replace(temp_path, log_path)
    finally:
        # 正常替换后临时路径已不存在；异常时则清理遗留临时文件。
        temp_path.unlink(missing_ok=True)

    print(
        f"Removed {len(urls) - len(missing)} URL(s) from {log_path} "
        f"({stable_path_matches} by stable path); JSON is valid."
    )
    return 0


if __name__ == "__main__":
    # 仅在脚本被直接执行时运行主流程，作为模块导入时不会产生文件修改。
    raise SystemExit(main())

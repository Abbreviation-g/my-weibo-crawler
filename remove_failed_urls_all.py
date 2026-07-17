#!/usr/bin/env python3
"""批量调用 remove_failed_urls.py，处理 F:\\weibo_log 下符合条件的一级子目录。

只有同时存在 failed.list 与 pics_videos.log 的目录才会被处理。每个目录独立
执行：某一个目录失败不会阻止后续目录继续处理，最后会输出完整汇总。
"""

from __future__ import annotations

import argparse
from pathlib import Path
import subprocess
import sys


# 未传入根目录参数时使用的默认微博日志目录。
DEFAULT_ROOT = Path(r"F:\weibo_log")
# 与本脚本位于同一目录的单目录处理脚本，避免依赖当前工作目录。
SCRIPT_PATH = Path(__file__).with_name("remove_failed_urls.py")
# 只有同时具备这两个文件的目录，才是可安全调用单目录脚本的目标。
REQUIRED_FILES = ("failed.list", "pics_videos.log")


def is_eligible(directory: Path) -> bool:
    """判断目录是否具备单目录删除脚本所需的全部文件。

    此方法不会读取或修改目录中的文件，仅用于筛选批处理范围，避免将无关目录
    传递给 remove_failed_urls.py。
    """
    # 必须先是目录，再逐项验证 failed.list 与 pics_videos.log 都是普通文件。
    return directory.is_dir() and all((directory / name).is_file() for name in REQUIRED_FILES)


def main() -> int:
    """解析批处理参数，逐目录调用删除脚本，并输出成功、跳过和失败统计。"""
    # 根目录可省略；省略时处理预设的 F:\weibo_log。
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "root",
        nargs="?",
        type=Path,
        default=DEFAULT_ROOT,
        help=f"Directory whose direct subfolders are processed (default: {DEFAULT_ROOT})",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Pass --dry-run to remove_failed_urls.py; do not modify any log files",
    )
    args = parser.parse_args()

    # 转为绝对路径，保证后续子进程接收到稳定、清晰的目录位置。
    root = args.root.resolve()
    if not root.is_dir():
        parser.error(f"Directory not found: {root}")
    # 先确认被调用脚本存在，防止循环开始后每个目录都因同一原因失败。
    if not SCRIPT_PATH.is_file():
        parser.error(f"Required script not found: {SCRIPT_PATH}")

    # 按不区分大小写的目录名排序，使每次批处理输出顺序保持稳定。
    eligible = [child for child in sorted(root.iterdir(), key=lambda path: path.name.casefold()) if is_eligible(child)]
    # 仅统计一级子目录中的跳过项；文件和更深层目录不会参与本次批处理。
    skipped = [child for child in sorted(root.iterdir(), key=lambda path: path.name.casefold()) if child.is_dir() and child not in eligible]
    # 保存子进程返回非零状态的目录，供最后统一提示与设置退出码。
    failures: list[Path] = []

    for directory in eligible:
        # 使用当前 Python 解释器调用单目录脚本，避免系统中多个 Python 版本不一致。
        command = [sys.executable, str(SCRIPT_PATH), str(directory)]
        if args.dry_run:
            # 预演参数原样透传，确保批量预演不会修改任何 pics_videos.log。
            command.append("--dry-run")
        print(f"\nProcessing: {directory}")
        # 不使用 check=True：单个目录出错时记录失败并继续处理其余目录。
        result = subprocess.run(command, check=False)
        if result.returncode:
            failures.append(directory)

    # 汇总与最终退出码都在全部目录尝试完成后产生，便于自动化任务判断结果。
    print(f"\nProcessed: {len(eligible)}; skipped: {len(skipped)}; failed: {len(failures)}")
    if failures:
        print("Failed directories:")
        for directory in failures:
            print(f"  {directory}")
        # 只要存在失败目录，就向调用方返回非零状态。
        return 1
    # 所有可处理目录均成功时返回零。
    return 0


if __name__ == "__main__":
    # 仅直接执行脚本时启动批量流程；被其他 Python 模块导入时不会自动处理文件。
    raise SystemExit(main())

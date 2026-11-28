#!/usr/bin/env bash
# 테스트 결과 XML 의 전체 · 건너뜀 개수 요약, 건너뛴 테스트가 있으면 실패
set -euo pipefail

dir="${1:-build/test-results/test}"
summary="${GITHUB_STEP_SUMMARY:-}"

shopt -s nullglob
files=("$dir"/TEST-*.xml)
if [ ${#files[@]} -eq 0 ]; then
  echo "테스트 결과 없음: $dir" >&2
  exit 1
fi

total=0
skipped=0
skipped_suites=()
for f in "${files[@]}"; do
  t=$(grep -o -m1 'tests="[0-9]*"' "$f" | grep -o '[0-9]*')
  s=$(grep -o -m1 'skipped="[0-9]*"' "$f" | grep -o '[0-9]*')
  total=$((total + t))
  skipped=$((skipped + s))
  if [ "$s" -gt 0 ]; then
    name=$(basename "$f" .xml)
    skipped_suites+=("${name#TEST-} ($s)")
  fi
done

report() {
  echo "### 백엔드 테스트"
  echo ""
  echo "전체 $total · 건너뜀 $skipped"
  for s in "${skipped_suites[@]}"; do
    echo "- 건너뜀: $s"
  done
}
report
if [ -n "$summary" ]; then
  report >> "$summary"
fi

if [ "$skipped" -gt 0 ]; then
  echo "건너뛴 테스트 $skipped 개" >&2
  exit 1
fi

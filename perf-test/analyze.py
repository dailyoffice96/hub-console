import csv, sys, statistics

def analyze(path, label_filter=None):
    rows = []
    with open(path, newline='', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for r in reader:
            if label_filter and label_filter not in r['label']:
                continue
            rows.append(r)
    if not rows:
        print(f"{path}: no matching rows for filter={label_filter}")
        return
    elapsed = [int(r['elapsed']) for r in rows]
    errors = [r for r in rows if r['success'] != 'true']
    n = len(rows)
    ts_start = min(int(r['timeStamp']) for r in rows)
    ts_end_candidates = [int(r['timeStamp']) + int(r['elapsed']) for r in rows]
    ts_end = max(ts_end_candidates)
    duration_sec = max((ts_end - ts_start) / 1000.0, 0.001)
    avg = statistics.mean(elapsed)
    sorted_e = sorted(elapsed)
    def pct(p):
        idx = min(int(len(sorted_e) * p), len(sorted_e) - 1)
        return sorted_e[idx]
    print(f"=== {path} (filter={label_filter}) ===")
    print(f"  Samples     : {n}")
    print(f"  Errors      : {len(errors)} ({len(errors)/n*100:.2f}%)")
    print(f"  Average(ms) : {avg:.1f}")
    print(f"  Median(ms)  : {pct(0.5)}")
    print(f"  90% Line(ms): {pct(0.9)}")
    print(f"  95% Line(ms): {pct(0.95)}")
    print(f"  Min/Max(ms) : {min(elapsed)}/{max(elapsed)}")
    print(f"  Duration(s) : {duration_sec:.2f}")
    print(f"  Throughput  : {n/duration_sec:.2f} req/s")
    if errors:
        from collections import Counter
        codes = Counter(r['responseCode'] for r in errors)
        print(f"  Error codes : {dict(codes)}")
    print()

if __name__ == '__main__':
    path = sys.argv[1]
    label_filter = sys.argv[2] if len(sys.argv) > 2 else None
    analyze(path, label_filter)

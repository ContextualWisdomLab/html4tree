import csv

try:
    with open("build/reports/jacoco/test/jacocoTestReport.csv", "r") as f:
        reader = csv.DictReader(f)
        for row in reader:
            if int(row["INSTRUCTION_MISSED"]) > 0:
                total = int(row["INSTRUCTION_COVERED"]) + int(row["INSTRUCTION_MISSED"])
                print(f"{row['PACKAGE']}.{row['CLASS']}.{row['METHOD']}: Missed {row['INSTRUCTION_MISSED']}/{total} instructions")
except Exception as e:
    print(f"Error: {e}")

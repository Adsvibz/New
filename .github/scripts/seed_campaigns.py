#!/usr/bin/env python3
"""Test-only fixture from the user-supplied AI Studio screenshot; never shipped to the app."""
import json
import time
import xml.sax.saxutils as xml
from pathlib import Path

campaigns = [
    ("Enterprise VIP Outbound Q4", 420, 284, 218, 7),
    ("Healthcare Provider Wave 2", 650, 488, 412, 8),
    ("SaaS Annual Renewals 2026", 180, 72, 58, 24),
    ("Real Estate Inbound Leads Oct", 310, 310, 245, 48),
    ("Auto-Service Maintenance Recall", 520, 0, 0, 72),
]
now_ms = int(time.time() * 1000)
data = []
for ci, (name, total, called, answered, age) in enumerate(campaigns):
    entries = []
    for i in range(total):
        status = 2 if i < answered else (3 if i < called else 0)
        entries.append({
            "id": f"visual-{ci}-{i}",
            "name": f"Test Contact {i+1}",
            "number": f"+1202{ci:02d}{i:06d}",
            "status": status,
            "attempts": 1 if status else 0,
            "duration": 100 if status == 2 else 0,
            "calledAt": now_ms - age * 3600000 if status else 0,
            "stage": 0,
            "feedback": ""
        })
    data.append({"id": f"visual-{ci}", "name": name, "entries": entries, "sim": -1})
prefs = '<?xml version="1.0" encoding="utf-8" standalone="yes" ?><map>' + (
    '<boolean name="welcomed" value="true" /><string name="lists">'
    + xml.escape(json.dumps(data, separators=(",", ":")))
    + '</string></map>'
)
Path("o2dialer.xml").write_text(prefs, encoding="utf-8")
print(f"Generated {len(data)} fixture campaigns, {sum(len(x['entries']) for x in data)} contacts")

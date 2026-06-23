# exchange-differences (Różnice kursowe)

Calculates the exchange-rate gain/loss between the NBP table-A rates on the invoice date and the
payment date, and prints/exports a summary label. Part of the [r4_tech tools](../README.md) suite.

## Run

```bash
mvn -DskipTests install                  # once (from the repo root)
mvn -pl exchange-differences javafx:run
```

## Package

This tool ships in the combined Windows installer (one `setup.exe`, pick which tools to install,
shared Java runtime). Build it from the repo root — see [packaging/README.md](../packaging/README.md):

```powershell
pwsh -File packaging\build-installer.ps1
```

## Data files

Needs `waluty.csv` (currency codes); a copy ships at the module root. For a packaged app, put it
next to the executable or set `-Dr4tech.dataDir=<dir>`. See the
[root README](../README.md#data-files-csv) for the full lookup order.

## Note on label printing

Label printing targets a hardcoded Xprinter (`XP-350B`/`XP-420B`). The print routine is guarded so a
missing printer shows a dialog instead of crashing, but the label layout itself (`LabelPrint`) was
left as-is pending verification on the physical printer.

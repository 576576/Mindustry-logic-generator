# CLI Reference

## Usage

```bash
java -jar mdtc-[version]-Cli.jar [options]
```

## Options

| Flag | Long | Argument | Description |
|------|------|----------|-------------|
| `-i` | `--file` | `<path>` | Input file (`.mdtc` / `.mdtcode` / `.libmdtc`) |
| `-o` | `--output` | `<path>` | Output path (default: auto-derived from input) |
| `-f` | `--format` | — | Format source file after compilation |
| `-fo` | `--format-only` | — | Only format, no compilation |
| `-oo` | `--open-out` | — | Reveal output file in file explorer |
| `-gpc` | `--generate-prime-code` | `<0\|1\|2>` | Generate intermediate code (debug) |
| `-v` | `--version` | — | Show version info |

## Examples

```bash
# Compile .mdtc → .mdtcode (auto-names output)
java -jar mdtc-[version]-Cli.jar -i sample_cases/case1.mdtc

# Compile + format source + open output
java -jar mdtc-[version]-Cli.jar -i sample_cases/case1.mdtc -f -oo

# Decompile .mdtcode → .mdtc + prime code
java -jar mdtc-[version]-Cli.jar -i sample_cases/case4.mdtcode -f -gpc 2

# Custom output path
java -jar mdtc-[version]-Cli.jar -i sample_cases/case5.mdtc -o output/custom.mdtcode

# Format a library file in-place
java -jar mdtc-[version]-Cli.jar -i modules/example.libmdtc -fo
```

## File Type Auto-Detection

The tool detects the operation from the input file extension:

| Input Extension | Operation |
|-----------------|-----------|
| `.mdtc` | Compile → `.mdtcode` |
| `.mdtcode` | Decompile → `.mdtc` |
| `.libmdtc` | Format-only |

## Prime Code Levels

`-gpc` generates intermediate representations for debugging:

| Level | Output |
|-------|--------|
| 1 | Source after function inlining, repeat expansion, and formatting |
| 2 | Source after full decompilation of jump/link structures |

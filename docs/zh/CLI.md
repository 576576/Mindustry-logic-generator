# CLI 参考

> [← 返回 README](README.md)

## 用法

```bash
java -jar mdtc-[version]-Cli.jar [选项]
```

## 选项

| 参数 | 完整写法 | 值 | 说明 |
|------|----------|-----|------|
| `-i` | `--file` | `<路径>` | 输入文件（`.mdtc` / `.mdtcode` / `.libmdtc`） |
| `-o` | `--output` | `<路径>` | 输出路径（默认自动推导） |
| `-f` | `--format` | — | 编译后格式化源码 |
| `-fo` | `--format-only` | — | 仅格式化，不编译 |
| `-oo` | `--open-out` | — | 在资源管理器中打开输出文件 |
| `-v` | `--version` | — | 显示版本 |

## 示例

```bash
# 编译 .mdtc → .mdtcode（输出自动命名）
java -jar mdtc-[version]-Cli.jar -i sample_cases/case1.mdtc

# 编译 + 格式化 + 打开输出
java -jar mdtc-[version]-Cli.jar -i sample_cases/case1.mdtc -f -oo

# 反编译 .mdtcode → .mdtc
java -jar mdtc-[version]-Cli.jar -i sample_cases/case4.mdtcode -f

# 指定输出路径
java -jar mdtc-[version]-Cli.jar -i sample_cases/case5.mdtc -o output/custom.mdtcode

# 原地格式化库文件
java -jar mdtc-[version]-Cli.jar -i modules/example.libmdtc -fo
```

## 文件类型自动识别

工具根据输入文件的扩展名自动判断操作：

| 输入扩展名 | 操作 |
|------------|------|
| `.mdtc` | 编译 → `.mdtcode` |
| `.mdtcode` | 反编译 → `.mdtc` |
| `.libmdtc` | 仅格式化 |

## 展开后代码输出

每次编译都会在输入文件旁额外生成一份 `import`/`function`/`repeat` 展开后的
中间代码（`.d.mdtc` 临时文件），例如 `case1.mdtc` → `case1.d.mdtc`。

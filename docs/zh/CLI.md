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
| `-gpc` | `--generate-prime-code` | `<0\|1\|2>` | 生成中间代码（调试用） |
| `-v` | `--version` | — | 显示版本 |

## 示例

```bash
# 编译 .mdtc → .mdtcode（输出自动命名）
java -jar mdtc-[version]-Cli.jar -i sample_cases/case1.mdtc

# 编译 + 格式化 + 打开输出
java -jar mdtc-[version]-Cli.jar -i sample_cases/case1.mdtc -f -oo

# 反编译 .mdtcode → .mdtc + 中间代码
java -jar mdtc-[version]-Cli.jar -i sample_cases/case4.mdtcode -f -gpc 2

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

## 中间代码级别

`-gpc` 生成用于调试的中间表示：

| 级别 | 输出内容 |
|------|---------|
| 1 | 函数内联 + repeat 展开 + 格式化后的源码 |
| 2 | 完整反编译跳跃/链接结构后的源码 |

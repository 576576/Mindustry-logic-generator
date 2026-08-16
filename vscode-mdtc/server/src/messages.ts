// 消息本地化:按客户端 locale 选择中文/英文(不支持时回退 en)

let zh = false;

export function setLocale(locale: string | undefined | null): void {
  zh = !!locale && locale.toLowerCase().startsWith("zh");
}

export function isZh(): boolean {
  return zh;
}

/** 负数守卫警告(按 locale) */
export function negativeWarning(token: string): string {
  const tail = token.substring(1);
  return isZh()
    ? `负数 "${token}" 未被 () 包裹;建议写成 "(${token})" 或使用空格减法 " - ${tail}"`
    : `Negative "${token}" is not wrapped in parentheses; use "(${token})" or spaced subtraction " - ${tail}"`;
}

/** 括号语法错误(按 locale) */
export function bracketError(ch: string): string {
  return isZh()
    ? `语法错误:标记 "${ch}" 无效,请删除该标记`
    : `Syntax error on token "${ch}", delete this token`;
}

export type MdtcodeErrorKind = "unknownInstr" | "jumpRange" | "labelNotFound" | "unknownOp";

/** mdtcode 诊断错误(按 locale) */
export function mdtcodeError(kind: MdtcodeErrorKind, arg: string): string {
  if (isZh()) {
    switch (kind) {
      case "unknownInstr": return `未知指令: ${arg}`;
      case "jumpRange": return `跳转目标超出范围: jump ${arg}`;
      case "labelNotFound": return `标签未定义: ${arg}`;
      case "unknownOp": return `未知运算符: ${arg}`;
    }
  }
  switch (kind) {
    case "unknownInstr": return `Unknown instruction: ${arg}`;
    case "jumpRange": return `Jump target out of range: jump ${arg}`;
    case "labelNotFound": return `Label not defined: ${arg}`;
    case "unknownOp": return `Unknown operator: ${arg}`;
  }
  return arg;
}

/** 编译异常兜底(按 locale) */
export function compileException(err: unknown): string {
  const detail = err instanceof Error ? err.message : String(err);
  return isZh() ? `编译异常: ${detail}` : `Compile exception: ${detail}`;
}

/** 翻译编译器产生的已知消息(zh 时);未知消息原样返回 */
export function translate(msg: string): string {
  if (!isZh()) return msg;
  let s = msg;
  s = s.replace(/Syntax error on token "\("", delete this token/, () => "语法错误:标记 \"(\" 无效,请删除该标记");
  s = s.replace(/Syntax error on token "\{", delete this token/, () => "语法错误:标记 \"{\" 无效,请删除该标记");
  s = s.replace(/Syntax error on token "\}", delete this token/, () => "语法错误:标记 \"}\" 无效,请删除该标记");
  s = s.replace(/Syntax error on token "else", delete this token/, () => "语法错误:标记 \"else\" 无效,请删除该标记");
  s = s.replace("Error: for() content not match", "错误:for() 内容不匹配");
  s = s.replace("Error: repeat() not enough infos", "错误:repeat() 参数不足");
  const jump = s.match(/^jump\(\) tag not found: (.+?) — replaced with DEFAULT/);
  if (jump) s = `jump() 标签未找到: ${jump[1].trim()},已替换为 DEFAULT`;
  if (s.startsWith("chain warning:")) {
    s = "链式警告:" + s.substring("chain warning:".length)
      .replace("unknown chain key", "未知链键")
      .replace("(ignored)", "(已忽略)");
  }
  return s;
}

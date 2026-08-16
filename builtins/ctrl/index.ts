/**
 * Ctrl 大类注册(顺序 = 匹配顺序;raw 必须最后,否则吞掉 draw( 等)。
 * 匹配符号由大类定义:裸指令名 + "("。
 */
namespace Builtins {
  export namespace Ctrl {
    export const category: Registry.CategorySpec = {
      keyOf: function (name) { return name + '('; },
      defs: [
        print, printchar, format, wait, stop, end,
        ubind, uctrl, ushoot, draw, jump, jump2, printf, tag, raw
      ]
    };
  }
}

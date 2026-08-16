/**
 * Dot 大类注册
 * 匹配符号由大类定义:裸指令名 + "." + "("。
 */
namespace Builtins {
  export namespace Dot {
    export const category: Registry.CategorySpec = {
      keyOf: function (name) { return '.' + name + '('; },
      defs: [sensor, read, orElse]
    };
  }
}

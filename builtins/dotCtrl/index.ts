/**
 * DotCtrl 大类注册
 * 匹配符号由大类定义:裸指令名 + "." + "("。
 */
namespace Builtins {
  export namespace DotCtrl {
    export const category: Registry.CategorySpec = {
      keyOf: function (name) { return '.' + name + '('; },
      defs: [ctrl, enable, config, color, shoot, ulocate, unpack, pflush, dflush, write]
    };
  }
}

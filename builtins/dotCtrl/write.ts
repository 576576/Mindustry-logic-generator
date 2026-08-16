/**
 * .write — 内存写指令。
 *
 * 语法:`<block>.write(<内容>,<单元号>)`
 * 输出:`write <w0|null> <block> <w1|0>`
 * 反编译:`write ` → `<block>.write(<内容>[,<单元号>])`(单元号为 0 省略)
 */

namespace Builtins {
  export namespace DotCtrl {
    import H = Builtins.Helpers;
    import D = Builtins.Domain;

    export const write: InstrDef = {
      desc: '写入存储单元',
      key: 'write',
      params: ['内容', '单元号'],
      compile: function (s, ctx) {
        const w = ctx.parts(s);
        return 'write ' + H.getOr(w, 0, D.VAL_NUL) + ' ' + ctx.block() + ' ' + H.getOr(w, 1, D.VAL_0);
      },
      restore: function (s) {
        const p = s.split(' ');
        const content = p[2] === '0' ? p[0] : p[0] + ',' + p[2];
        return p[1] + '.write(' + content + ')';
      }
    };
  }
}

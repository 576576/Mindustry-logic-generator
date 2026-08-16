/**
 * .sensor — 传感器读取(表达式,结果变量在等号左侧)。
 *
 * 语法:`<结果> = <block>.sensor(<属性>)`
 * 输出:`sensor mid.<ref> <block> <s>`;调用方随后把 mid.<ref> 代入表达式并递增 ref
 * 示例:`heat = reactor.sensor(@heat)`
 * 反编译:`sensor ` → `<结果>=<block>.sensor(<属性>)`
 */

namespace Builtins {
  export namespace Dot {
    export const sensor: InstrDef = {
      key: 'sensor',
      mcode: 'sensor',
      compile: function (s, ctx) { return 'sensor ' + ctx.mid() + ' ' + ctx.block() + ' ' + s; },
      restore: function (s) {
        const p = s.split(' ');
        return p[0] + '=' + p[1] + '.sensor(' + p[2] + ')';
      }
    };
  }
}

/**
 * .enable — 控制指令(规范见 docs/instructions/dot.md)
 * mcode 为共享指令字 "control",按 "enabled" 分派。
 */
namespace Builtins {
  export namespace DotCtrl {
    import H = Builtins.Helpers;

    export const enable: InstrDef = {
      key: 'enable',
      mcode: 'control',
      mcodeSelect: ['enabled'],
      compile: function (s, ctx) { return 'control enabled ' + ctx.block() + ' ' + H.padZero(4, ctx.parts(s)); },
      restore: function (s) {
        const parts = H.splitInto(s, 3);
        const ps = (parts[2] == null ? '' : parts[2]).split(' ');
        return parts[1] + '.enable(' + (ps[0] == null ? '' : ps[0]) + ')';
      }
    };
  }
}

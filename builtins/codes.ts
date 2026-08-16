/**
 * 指令码表(规范见 docs/instructions/operators.md 的"指令码表")。
 */
namespace Builtins {
  export namespace Codes {
    export const CTRL: string[] = [
      'print(', 'printchar(', 'format(', 'wait(', 'stop(', 'end(',
      'ubind(', 'uctrl(', 'ushoot(', 'draw(', 'jump(', 'jump2(',
      'printf(', 'tag(', 'raw('
    ];
    export const DOT_CTRL: string[] = [
      '.ctrl(', '.enable(', '.config(', '.color(', '.shoot(',
      '.ulocate(', '.unpack(', '.pflush(', '.dflush(', '.write('
    ];
    export const DOT: string[] = ['.sensor(', '.read(', '.orElse('];
    export const DOT_ALL: string[] = DOT_CTRL.concat(DOT);
    export const DOT_REDUCED: string[] = DOT_ALL.map(function (k) { return k.slice(0, -1); });
  }
}

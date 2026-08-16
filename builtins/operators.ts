/**
 * 运算符表(规范见 docs/instructions/operators.md)。
 * 数组顺序 = 词法匹配顺序,不可调整。
 */
namespace Builtins {
  export namespace Operators {
    export interface OpDef {
      name: string;
      value: string;
      priority: number;
      /** 前后须有空白(或行边界)才匹配,避免吞并连字符标识符(如 phase-fabric) */
      spaced?: boolean;
      /** 附加词法值(同 name/priority,兼容旧写法;主 value 仍用于还原输出) */
      values?: string[];
    }
    export const list: OpDef[] = [
      { name: 'add', value: '+', priority: 4 },
      { name: 'sub', value: '-', priority: 4, spaced: true, values: ['.-'] },
      { name: 'mul', value: '*', priority: 5 },
      { name: 'idiv', value: '//', priority: 5 },
      { name: 'div', value: '/', priority: 5 },
      { name: 'emod', value: '%%', priority: 5 },
      { name: 'mod', value: '.%', priority: 5 },
      { name: 'pow', value: '.^', priority: 7 },
      { name: 'strictEqual', value: '===', priority: 3 },
      { name: 'equal', value: '==', priority: 3 },
      { name: 'notEqual', value: '!=', priority: 3 },
      { name: 'land', value: '&&', priority: 2 },
      { name: 'greaterThanEq', value: '>=', priority: 3 },
      { name: 'lessThanEq', value: '<=', priority: 3 },
      { name: 'ushr', value: '>>>', priority: 5 },
      { name: 'shr', value: '>>', priority: 5 },
      { name: 'shl', value: '<<', priority: 5 },
      { name: 'xor', value: '^', priority: 2 },
      { name: 'greaterThan', value: '>', priority: 3 },
      { name: 'lessThan', value: '<', priority: 3 },
      { name: 'and', value: '&', priority: 2 },
      { name: 'or', value: '|', priority: 2 },
      { name: 'lbracket', value: '(', priority: 10 },
      { name: 'rbracket', value: ')', priority: 10 },
      { name: 'set', value: '=', priority: 1 },
      { name: 'always', value: 'always', priority: 1 },
      { name: 'never', value: 'never', priority: 1 }
    ];
    /** 词法值 → 运算符名(midOpKeysMap) */
    export const byValue: { [key: string]: string } = {};
    /** 运算符名 → 词法值(midOpValueMap) */
    export const byName: { [key: string]: string } = {};
    /** 词法值 → 优先级(midOpPriorityMap) */
    export const priority: { [key: string]: number } = {};
    for (const o of list) {
      for (const v of [o.value, ...(o.values ?? [])]) {
        byValue[v] = o.name;
        priority[v] = o.priority;
      }
      byName[o.name] = o.value;
    }
    /** 运算符别名表 */
    export const alias: { [key: string]: string } = {
      log10: 'lg',
      log: 'ln',
      logn: 'log'
    };
    /** 条件反转表 */
    export const reverse: { [key: string]: string } = {
      notEqual: 'equal',
      equal: 'notEqual',
      strictEqual: 'notEqual',
      lessThan: 'greaterThanEq',
      lessThanEq: 'greaterThan',
      greaterThan: 'lessThanEq',
      greaterThanEq: 'lessThan',
      always: 'never',
      never: 'always'
    };
    /** 指令输出偏移表 */
    export const offset: { [key: string]: number } = {
      op: 2,
      sensor: 1,
      getlink: 1,
      radar: 7,
      uradar: 7,
      lookup: 2,
      packcolor: 1,
      read: 1,
      set: 1,
      select: 1
    };
    /** sub 运算符词法值(负数字面量展开用) */
    export const SUB_VALUE = '-';
  }
}

/**
 * 通用发射辅助(与 Java Utils 的对应算法保持一致)。
 */
namespace Builtins {
  export namespace Helpers {
    import O = Builtins.Operators;

    /** 参数填充(等同 Utils.formatParams:补到 n 项,空/缺省用 def,空格连接) */
    export function pad(def: string, n: number, parts: string[]): string {
      const out: string[] = [];
      for (let i = 0; i < n; i++) {
        const v = i < parts.length ? parts[i] : '';
        out.push(v == null || v === '' ? def : v);
      }
      return out.join(' ');
    }
    /** 以 '0' 为缺省填充(等同 Utils.padParams(n, s)) */
    export function padZero(n: number, parts: string[]): string {
      return pad('0', n, parts);
    }
    /** 链式参数取值(等同 WrappedMap.getOrDefault) */
    export function chainGet(m: { [key: string]: string }, key: string, def: string): string {
      const v = m[key];
      return v == null || v === '' ? def : v;
    }
    /** 安全取数组项(等同 WrappedList.getOrDefault:trim 且空→def) */
    export function getOr(parts: string[], i: number, def: string): string {
      if (i < 0 || i >= parts.length) return def;
      const v = parts[i].trim();
      return v === '' ? def : v;
    }
    /** 取末项(等同 getOr(parts, length-1, def)) */
    export function getLastOr(parts: string[], def: string): string {
      return getOr(parts, parts.length - 1, def);
    }
    /** 去尾部缺省并转逗号分隔(等同 Utils.reduceParams(String, String)) */
    export function reduce(def: string, s: string): string {
      let r = s;
      while (def.length > 0 && r.length >= def.length && r.lastIndexOf(def) === r.length - def.length) {
        r = r.slice(0, r.length - def.length).trim();
      }
      return r.replace(/ /g, ',');
    }
    /** 数组版 reduce(等同 Utils.reduceParams(def, n, params...)) */
    export function reduceArr(def: string, n: number, parts: string[]): string {
      const filled: string[] = [];
      for (let i = 0; i < n; i++) {
        filled.push(i < parts.length && parts[i] != null ? parts[i] : def);
      }
      return reduce(def, filled.join(' '));
    }
    /** 条件还原(等同 Utils.reduceCondition) */
    export function reduceCondition(condition: string): string {
      const params = condition.split(' ');
      if (params[0] === 'always') return 'always';
      if (params[0] === 'never') return 'never';
      const opVal = O.byName[params[0]];
      const p1 = params[1] == null ? '' : params[1];
      const p2 = params[2] == null ? '' : params[2].trim();
      return p1 + (opVal == null ? 'null' : opVal) + p2;
    }
    /** 条件提取(等同 Utils.getCondition) */
    export function getCondition(line: string): string {
      const defaultCondition = 'always 0 0';
      const params = line.split(' ');
      if (params.length === 0) return defaultCondition;
      const key = params[0];
      if (key === 'op') {
        if (!O.reverse[params[1]]) {
          const target = params[O.offset['op']];
          return 'notEqual ' + (target == null ? '' : target) + ' 0';
        }
        return [params[1], params[3] == null ? '' : params[3], params[4] == null ? '' : params[4]].join(' ');
      } else if (O.offset[key] != null) {
        const target = params[O.offset[key]];
        return 'notEqual ' + (target == null ? '' : target) + ' 0';
      }
      return defaultCondition;
    }
    /** 条件反转(等同 Utils.reverseCondition) */
    export function reverseCondition(line: string): string {
      const parts = line.split(' ');
      for (let i = 0; i < parts.length; i++) {
        const rev = O.reverse[parts[i]];
        if (rev != null) parts[i] = rev;
      }
      return parts.join(' ');
    }
    /** 拆分前 n 段(等同 Java String.split(" ", n)) */
    export function splitInto(s: string, n: number): string[] {
      const out: string[] = [];
      let rest = s;
      for (let i = 0; i < n - 1; i++) {
        const idx = rest.indexOf(' ');
        if (idx === -1) { out.push(rest); rest = ''; break; }
        out.push(rest.slice(0, idx));
        rest = rest.slice(idx + 1);
      }
      if (rest !== '' || out.length < n) out.push(rest);
      return out;
    }
  }
}

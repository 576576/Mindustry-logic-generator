"use strict";
/**
 * 领域数据(规范见 docs/instructions/domain.md)。
 * 静态目录;模组运行期由 BuiltinDomain.init() 合并 Vars.content 游戏内容名。
 */
var Builtins;
(function (Builtins) {
    var Domain;
    (function (Domain) {
        /** ulocate 可定位建筑类型(第 0 项为默认) */
        Domain.BUILDING_TYPES = [
            'core', 'storage', 'generator', 'turret', 'factory',
            'repair', 'battery', 'reactor', 'drill', 'shield'
        ];
        /** 定位模式(第 0 项为默认) */
        Domain.LOCATE_TYPES = ['ore', 'building'];
        /** lookup 内容类型(第 0 项为默认) */
        Domain.LOOKUP_TYPES = ['block', 'unit', 'item', 'liquid', 'team'];
        /** 链式参数键 */
        Domain.CHAIN_KEYS = ['main', 'target', 'when', 'order', 'sort', 'ore', 'building', 'enemy'];
        /** .ctrl() 默认控制类型 */
        Domain.CONTROL_DEFAULT = 'enabled';
        /** radar/uradar 默认排序 */
        Domain.RADAR_SORT_DEFAULT = 'distance';
        /** 默认值常量 */
        Domain.VAL_0 = '0';
        Domain.VAL_1 = '1';
        Domain.VAL_AT = '@this';
        Domain.VAL_NUL = 'null';
        Domain.JUMP_DEFAULT = 'DEFAULT';
        Domain.RADAR_TARGET = 'enemy,any,any';
    })(Domain = Builtins.Domain || (Builtins.Domain = {}));
})(Builtins || (Builtins = {}));
/**
 * 运算符表(规范见 docs/instructions/operators.md)。
 * 数组顺序 = 词法匹配顺序,不可调整。
 */
var Builtins;
(function (Builtins) {
    var Operators;
    (function (Operators) {
        Operators.list = [
            { name: 'add', value: '+', priority: 4 },
            { name: 'sub', value: '.-', priority: 4 },
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
        Operators.byValue = {};
        /** 运算符名 → 词法值(midOpValueMap) */
        Operators.byName = {};
        /** 词法值 → 优先级(midOpPriorityMap) */
        Operators.priority = {};
        for (var _i = 0, list_1 = Operators.list; _i < list_1.length; _i++) {
            var o = list_1[_i];
            Operators.byValue[o.value] = o.name;
            Operators.byName[o.name] = o.value;
            Operators.priority[o.value] = o.priority;
        }
        /** 运算符别名表 */
        Operators.alias = {
            log10: 'lg',
            log: 'ln',
            logn: 'log'
        };
        /** 条件反转表 */
        Operators.reverse = {
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
        Operators.offset = {
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
        Operators.SUB_VALUE = '.-';
    })(Operators = Builtins.Operators || (Builtins.Operators = {}));
})(Builtins || (Builtins = {}));
/**
 * 指令码表(规范见 docs/instructions/operators.md 的"指令码表")。
 */
var Builtins;
(function (Builtins) {
    var Codes;
    (function (Codes) {
        Codes.CTRL = [
            'print(', 'printchar(', 'format(', 'wait(', 'stop(', 'end(',
            'ubind(', 'uctrl(', 'ushoot(', 'draw(', 'jump(', 'jump2(',
            'printf(', 'tag(', 'raw('
        ];
        Codes.DOT_CTRL = [
            '.ctrl(', '.enable(', '.config(', '.color(', '.shoot(',
            '.ulocate(', '.unpack(', '.pflush(', '.dflush(', '.write('
        ];
        Codes.DOT = ['.sensor(', '.read(', '.orElse('];
        Codes.DOT_ALL = Codes.DOT_CTRL.concat(Codes.DOT);
        Codes.DOT_REDUCED = Codes.DOT_ALL.map(function (k) { return k.slice(0, -1); });
    })(Codes = Builtins.Codes || (Builtins.Codes = {}));
})(Builtins || (Builtins = {}));
/**
 * 通用发射辅助(与 Java Utils 的对应算法保持一致)。
 */
var Builtins;
(function (Builtins) {
    var Helpers;
    (function (Helpers) {
        var O = Builtins.Operators;
        /** 参数填充(等同 Utils.formatParams:补到 n 项,空/缺省用 def,空格连接) */
        function pad(def, n, parts) {
            var out = [];
            for (var i = 0; i < n; i++) {
                var v = i < parts.length ? parts[i] : '';
                out.push(v == null || v === '' ? def : v);
            }
            return out.join(' ');
        }
        Helpers.pad = pad;
        /** 以 '0' 为缺省填充(等同 Utils.padParams(n, s)) */
        function padZero(n, parts) {
            return pad('0', n, parts);
        }
        Helpers.padZero = padZero;
        /** 链式参数取值(等同 WrappedMap.getOrDefault) */
        function chainGet(m, key, def) {
            var v = m[key];
            return v == null || v === '' ? def : v;
        }
        Helpers.chainGet = chainGet;
        /** 安全取数组项(等同 WrappedList.getOrDefault:trim 且空→def) */
        function getOr(parts, i, def) {
            if (i < 0 || i >= parts.length)
                return def;
            var v = parts[i].trim();
            return v === '' ? def : v;
        }
        Helpers.getOr = getOr;
        /** 取末项(等同 getOr(parts, length-1, def)) */
        function getLastOr(parts, def) {
            return getOr(parts, parts.length - 1, def);
        }
        Helpers.getLastOr = getLastOr;
        /** 去尾部缺省并转逗号分隔(等同 Utils.reduceParams(String, String)) */
        function reduce(def, s) {
            var r = s;
            while (def.length > 0 && r.length >= def.length && r.lastIndexOf(def) === r.length - def.length) {
                r = r.slice(0, r.length - def.length).trim();
            }
            return r.replace(/ /g, ',');
        }
        Helpers.reduce = reduce;
        /** 数组版 reduce(等同 Utils.reduceParams(def, n, params...)) */
        function reduceArr(def, n, parts) {
            var filled = [];
            for (var i = 0; i < n; i++) {
                filled.push(i < parts.length && parts[i] != null ? parts[i] : def);
            }
            return reduce(def, filled.join(' '));
        }
        Helpers.reduceArr = reduceArr;
        /** 条件还原(等同 Utils.reduceCondition) */
        function reduceCondition(condition) {
            var params = condition.split(' ');
            if (params[0] === 'always')
                return 'always';
            if (params[0] === 'never')
                return 'never';
            var opVal = O.byName[params[0]];
            var p1 = params[1] == null ? '' : params[1];
            var p2 = params[2] == null ? '' : params[2].trim();
            return p1 + (opVal == null ? 'null' : opVal) + p2;
        }
        Helpers.reduceCondition = reduceCondition;
        /** 条件提取(等同 Utils.getCondition) */
        function getCondition(line) {
            var defaultCondition = 'always 0 0';
            var params = line.split(' ');
            if (params.length === 0)
                return defaultCondition;
            var key = params[0];
            if (key === 'op') {
                if (!O.reverse[params[1]]) {
                    var target = params[O.offset['op']];
                    return 'notEqual ' + (target == null ? '' : target) + ' 0';
                }
                return [params[1], params[3] == null ? '' : params[3], params[4] == null ? '' : params[4]].join(' ');
            }
            else if (O.offset[key] != null) {
                var target = params[O.offset[key]];
                return 'notEqual ' + (target == null ? '' : target) + ' 0';
            }
            return defaultCondition;
        }
        Helpers.getCondition = getCondition;
        /** 条件反转(等同 Utils.reverseCondition) */
        function reverseCondition(line) {
            var parts = line.split(' ');
            for (var i = 0; i < parts.length; i++) {
                var rev = O.reverse[parts[i]];
                if (rev != null)
                    parts[i] = rev;
            }
            return parts.join(' ');
        }
        Helpers.reverseCondition = reverseCondition;
        /** 拆分前 n 段(等同 Java String.split(" ", n)) */
        function splitInto(s, n) {
            var out = [];
            var rest = s;
            for (var i = 0; i < n - 1; i++) {
                var idx = rest.indexOf(' ');
                if (idx === -1) {
                    out.push(rest);
                    rest = '';
                    break;
                }
                out.push(rest.slice(0, idx));
                rest = rest.slice(idx + 1);
            }
            if (rest !== '' || out.length < n)
                out.push(rest);
            return out;
        }
        Helpers.splitInto = splitInto;
    })(Helpers = Builtins.Helpers || (Builtins.Helpers = {}));
})(Builtins || (Builtins = {}));
/**
 * 注册表组装:由各指令的 InstrDef 双向映射派生 compile / decompile 注册表。
 * - key 为裸指令名;完整扫描键由大类 CategorySpec.keyOf 模板生成
 *   (ctrl/front 附加 "(",dotCtrl/dot 附加 "." + "(")
 * - compile 映射:完整键 → 处理器(带 chain 链键检查包装)
 * - decompile 映射:mcode + " " → 还原处理器(共享指令字按 mcodeSelect 分派)
 * - chain 表:完整键 → 合法链键列表(供测试与运行期检查)
 * - set / op 为语言机制级通用还原(由运算符表数据驱动)
 */
var Builtins;
(function (Builtins) {
    var Registry;
    (function (Registry) {
        var H = Builtins.Helpers;
        var O = Builtins.Operators;
        /** 从 chain 声明取缺省值(compile 与 restore 的单一默认值来源) */
        function chainDef(chain, key) {
            if (!chain)
                return '';
            for (var _i = 0, chain_1 = chain; _i < chain_1.length; _i++) {
                var c = chain_1[_i];
                if (c.key === key)
                    return c.def;
            }
            return '';
        }
        Registry.chainDef = chainDef;
        /** 链键白名单(首部 main 恒允许) */
        function allowedKeys(chain) {
            var m = { main: true };
            if (chain) {
                for (var _i = 0, chain_2 = chain; _i < chain_2.length; _i++) {
                    var c = chain_2[_i];
                    m[c.key] = true;
                }
            }
            return m;
        }
        /** 带链检查的 compile 包装:未知链键输出警告(不影响输出) */
        function wrapChain(def, fullKey) {
            if (!def.chain || def.chain.length === 0)
                return def.compile;
            return function (s, ctx) {
                var m = ctx.chain(s);
                var allow = allowedKeys(def.chain);
                for (var _i = 0, _a = Object.keys(m); _i < _a.length; _i++) {
                    var k = _a[_i];
                    if (!allow[k]) {
                        ctx.warn('chain warning: ' + fullKey + ' — unknown chain key "' + k + '" (ignored)');
                    }
                }
                return def.compile(s, ctx);
            };
        }
        Registry.wrapChain = wrapChain;
        /** 大类 → compile 映射(完整扫描键 → 包装后处理器) */
        function compileMap(spec) {
            var m = {};
            for (var _i = 0, _a = spec.defs; _i < _a.length; _i++) {
                var def = _a[_i];
                var fullKey = spec.keyOf(def.key);
                m[fullKey] = wrapChain(def, fullKey);
            }
            return m;
        }
        Registry.compileMap = compileMap;
        /** mcode 派生:显式 mcode 优先,否则裸 key 即 mdtcode 指令字 */
        function mcodeOf(def) {
            return def.mcode || def.key;
        }
        Registry.mcodeOf = mcodeOf;
        /** 共享指令字分派器:按行首 token 命中 mcodeSelect;无命中走兜底定义 */
        function dispatcher(defs) {
            var selectors = [];
            var fallback = null;
            for (var _i = 0, defs_1 = defs; _i < defs_1.length; _i++) {
                var d = defs_1[_i];
                if (d.mcodeSelect && d.mcodeSelect.length > 0)
                    selectors.push(d);
                else if (fallback == null)
                    fallback = d;
            }
            return function (s, ctx) {
                var idx = s.indexOf(' ');
                var token = idx === -1 ? s : s.slice(0, idx);
                for (var _i = 0, selectors_1 = selectors; _i < selectors_1.length; _i++) {
                    var d = selectors_1[_i];
                    if (d.mcodeSelect.indexOf(token) !== -1 && d.restore) {
                        return d.restore(s, ctx);
                    }
                }
                if (fallback != null && fallback.restore)
                    return fallback.restore(s, ctx);
                return '';
            };
        }
        /** defs → decompile 映射(mcode + " " → 还原处理器);仅收集带 restore 的定义 */
        function decompileMap(defs) {
            var groups = {};
            for (var _i = 0, defs_2 = defs; _i < defs_2.length; _i++) {
                var def = defs_2[_i];
                if (!def.restore)
                    continue;
                var mcode = mcodeOf(def);
                if (groups[mcode] == null)
                    groups[mcode] = [];
                groups[mcode].push(def);
            }
            var m = {};
            for (var _a = 0, _b = Object.keys(groups); _a < _b.length; _a++) {
                var mcode = _b[_a];
                var g = groups[mcode];
                m[mcode + ' '] = g.length === 1 ? g[0].restore : dispatcher(g);
            }
            return m;
        }
        Registry.decompileMap = decompileMap;
        /** 大类 → chain 表(完整扫描键 → 合法链键列表,顺序 = 声明顺序) */
        function chainTable(spec) {
            var t = {};
            for (var _i = 0, _a = spec.defs; _i < _a.length; _i++) {
                var d = _a[_i];
                if (d.chain && d.chain.length > 0) {
                    t[spec.keyOf(d.key)] = d.chain.map(function (c) { return c.key; });
                }
            }
            return t;
        }
        Registry.chainTable = chainTable;
        /** 赋值还原("x 1" → "x=1")——语言机制,非内置指令 */
        function setRestore(s) {
            return s.replace(' ', '=');
        }
        Registry.setRestore = setRestore;
        /** 运算符行还原("op <op> …" → mdtc 表达式)——由运算符表/别名表数据驱动 */
        function opRestore() {
            return function (s) {
                var p = s.split(' ');
                var operator0 = p[0];
                var result = p[1];
                var operator = operator0;
                var paramString = '';
                var val = O.byName[operator0];
                if (val != null) {
                    if (p.length < 4) {
                        return result + '=' + val + '(' + (p[2] == null ? '' : p[2]) + ')';
                    }
                    return result + '=' + (p[2] == null ? '' : p[2]) + ' ' + val + ' ' + (p[3] == null ? '' : p[3]);
                }
                else if (operator0 === 'logn' && p.length > 4 && p[4] === '2') {
                    operator = 'lb';
                    paramString = p[2] == null ? '' : p[2];
                }
                else {
                    operator = O.alias[operator0] == null ? operator0 : O.alias[operator0];
                    if (p.length <= 3) {
                        paramString = p[2] == null ? '' : p[2];
                    }
                    else if (operator === 'log') {
                        paramString = H.reduce('0', (p[3] == null ? '' : p[3]) + ' ' + (p[2] == null ? '' : p[2]));
                    }
                    else {
                        paramString = H.reduce('0', (p[2] == null ? '' : p[2]) + ' ' + (p[3] == null ? '' : p[3]));
                    }
                }
                return result + '=' + operator + '(' + paramString + ')';
            };
        }
        Registry.opRestore = opRestore;
    })(Registry = Builtins.Registry || (Builtins.Registry = {}));
})(Builtins || (Builtins = {}));
/**
 * draw — 固定填充指令。
 *
 * 语法:`draw(<类型>,<参数…>)`
 * 输出:`draw <pad(7, s)>`
 * 示例:`draw(clear)` → `draw clear 0 0 0 0 0 0`
 * 反编译:`draw ` → `draw(reduce(0, s))`(去掉尾部 0,逗号分隔)
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        var H = Builtins.Helpers;
        Ctrl.draw = {
            key: 'draw',
            compile: function (s, ctx) { return 'draw ' + H.padZero(7, ctx.parts(s)); },
            restore: function (s) { return 'draw(' + H.reduce('0', s) + ')'; }
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * end — 零参指令。
 *
 * 语法:`end()`
 * 输出:`end`(忽略参数)
 * 反编译:裸 `end` 行由 decompile 管道还原为 `end()`。
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        Ctrl.end = {
            key: 'end',
            compile: function () { return 'end'; }
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * format — 透传指令。
 *
 * 语法:`format(<值>)`
 * 输出:`format <s>`
 * 反编译:`format ` → `format(<s>)`
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        Ctrl.format = {
            key: 'format',
            compile: function (s) { return 'format ' + s; },
            restore: function (s) { return 'format(' + s + ')'; }
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * jump — 链式参数指令。
 *
 * 语法:`jump(<目标标签>).when(<条件>)`;when 缺省恒真
 * 链式键:main(目标标签,DEFAULT), when(条件,空)
 * 条件判定:
 * - when 多 token:子编译 whenExpr,产物非空时 condition 取末行(getCondition);
 *   非 `always 0 0` 弹出该行;`always 0 0` 且子表达式非空则 `notEqual <expr> 0`;产物行并入输出
 * - when 单 token:`always` → `always 0 0`;`never` → `notEqual 0 0`;其他 → `notEqual <whenExpr> 0`
 * - when 空:恒真
 * 输出:`jump <target> <condition>`
 * 反编译:`jump ` → `jump(<tag>)[.when(<cond>)]`(恒真省略 when)
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        var H = Builtins.Helpers;
        var R = Builtins.Registry;
        var CHAIN = [
            { key: 'main', def: 'DEFAULT' },
            { key: 'when', def: '' }
        ];
        Ctrl.jump = {
            key: 'jump',
            chain: CHAIN,
            compile: function (s, ctx) {
                var m = ctx.chain(s);
                var target = H.chainGet(m, 'main', R.chainDef(CHAIN, 'main'));
                var whenExpr = H.chainGet(m, 'when', R.chainDef(CHAIN, 'when'));
                var splitList = ctx.split(whenExpr);
                var condition;
                if (splitList.length > 1) {
                    var bc = ctx.compileSub(whenExpr);
                    if (bc.bash.length > 0) {
                        ctx.setRef(bc.stat);
                        condition = H.getCondition(bc.bash[bc.bash.length - 1]);
                        if (condition !== 'always 0 0') {
                            bc.bash = bc.bash.slice(0, bc.bash.length - 1);
                        }
                        else if (bc.expr !== '') {
                            condition = 'notEqual ' + bc.expr + ' 0';
                        }
                        ctx.bashAll(bc.bash);
                    }
                    else {
                        condition = 'always 0 0';
                    }
                }
                else if (splitList.length === 1) {
                    var first = splitList[0];
                    if (first === 'always')
                        condition = 'always 0 0';
                    else if (first === 'never')
                        condition = 'notEqual 0 0';
                    else
                        condition = 'notEqual ' + whenExpr + ' 0';
                }
                else {
                    condition = 'always 0 0';
                }
                return 'jump ' + target + ' ' + condition;
            },
            restore: function (s) {
                var alwaysConditions = ['0==0', 'always'];
                var parts = H.splitInto(s, 2);
                var condition = H.reduceCondition(parts[1]);
                if (alwaysConditions.indexOf(condition) !== -1)
                    condition = '';
                else
                    condition = '.when(' + condition + ')';
                return 'jump(' + parts[0] + ')' + condition;
            }
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * jump2 — 计算跳转;不直接产出行,无 restore(多行折叠由 decompile 管道处理)。
 *
 * 语法:`jump2(<表达式或增量表达式>)`
 * 输出:将 `s` 转为 `@counter=<s>`(单 token)或 `@counter=@counter<s>`(多 token),
 *   交给子编译后把产物行写入 bash 列表,返回空串。
 * 反编译:decompile 管道 convertJump2 把 `@counter=` 行折叠为 `jump2(...)`。
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        Ctrl.jump2 = {
            key: 'jump2',
            compile: function (s, ctx) {
                var compiled = ctx.compileSub(ctx.split(s).length > 1 ? '@counter=@counter' + s : '@counter=' + s);
                ctx.bashAll(compiled.bash);
                return '';
            }
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * print — 透传指令。
 *
 * 语法:`print(<内容>)`
 * 输出:`print <s>`
 * 反编译:`print ` → `print(<s>)`
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        Ctrl.print = {
            key: 'print',
            compile: function (s) { return 'print ' + s; },
            restore: function (s) { return 'print(' + s + ')'; }
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * printchar — 透传指令。
 *
 * 语法:`printchar(<码点>)`
 * 输出:`printchar <s>`
 * 反编译:`printchar ` → `printchar(<s>)`
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        Ctrl.printchar = {
            key: 'printchar',
            compile: function (s) { return 'printchar ' + s; },
            restore: function (s) { return 'printchar(' + s + ')'; }
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * printf — 多行展开指令;无 restore(多行 print+format 由 decompile 逐行还原)。
 *
 * 语法:`printf(<格式串>,<参数…>)`
 * 输出:参数不足 2 个时退化为 `print <s>`;否则产生多行:
 *   `print <p0>` + 每个后续参数一行 `format <pN>`。
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        Ctrl.printf = {
            key: 'printf',
            compile: function (s, ctx) {
                var w = ctx.parts(s);
                if (w.length < 2)
                    return 'print ' + s;
                var out = 'print ' + w[0];
                for (var i = 1; i < w.length; i++)
                    out += '\nformat ' + w[i];
                return out;
            }
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * raw — 原生透传指令;无 restore(未知指令行由 decompile 还原为 `raw("…")`)。
 *
 * 语法:`raw("<原生 mdtcode 指令>")`
 * 输出:`<s>`(原样透传,不做任何编译)
 * 注意:raw( 必须排在 draw( 之后(注册顺序即匹配顺序)。
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        Ctrl.raw = {
            key: 'raw',
            compile: function (s) { return s; }
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * stop — 零参指令。
 *
 * 语法:`stop()`
 * 输出:`stop`(忽略参数)
 * 反编译:裸 `stop` 行由 decompile 管道还原为 `stop()`。
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        Ctrl.stop = {
            key: 'stop',
            compile: function () { return 'stop'; }
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * tag — 标签指令;无 restore(`::` 行由 decompile 管道原样保留)。
 *
 * 语法:`tag(<标签名>)`
 * 输出:`::<s>`(标签行;由 convertLink 解析)
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        Ctrl.tag = {
            key: 'tag',
            compile: function (s) { return '::' + s; }
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * ubind — 透传指令。
 *
 * 语法:`ubind(<单位类型>)`
 * 输出:`ubind <s>`
 * 反编译:`ubind ` → `ubind(<s>)`
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        Ctrl.ubind = {
            key: 'ubind',
            compile: function (s) { return 'ubind ' + s; },
            restore: function (s) { return 'ubind(' + s + ')'; }
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * uctrl — 固定填充指令;mcode 为共享指令字 `ucontrol`(兜底)。
 *
 * 语法:`uctrl(<类型>,<参数…>)`
 * 输出:`ucontrol <pad(6, s)>`(参数按顶层逗号切分,填充到 6 项,缺省 `0`)
 * 示例:`uctrl(getBlock)` → `ucontrol getBlock 0 0 0 0 0`
 * 反编译:`ucontrol ` 非 target/targetp → `uctrl(reduce(0, 类型 参数…))`
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        var H = Builtins.Helpers;
        Ctrl.uctrl = {
            key: 'uctrl',
            mcode: 'ucontrol',
            compile: function (s, ctx) { return 'ucontrol ' + H.padZero(6, ctx.parts(s)); },
            restore: function (s) {
                var parts = H.splitInto(s, 2);
                return 'uctrl(' + H.reduce('0', parts[0] + ' ' + parts[1]) + ')';
            }
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * ushoot — 链式参数指令;mcode 为共享指令字 `ucontrol`,按 target/targetp 分派。
 *
 * 语法:`ushoot(<shoot>).target(<目标>|<x>,<y>)`;shoot 缺省 `1`,target 缺省 `@this`
 * 链式键:main(射击开关), target(目标;双参为坐标,单参为目标单位)
 * 输出:目标含逗号时 `ucontrol target <pad(5, tgt 逗号替换为空格, shoot)>`,
 *   否则 `ucontrol targetp <pad(5, tgt, shoot)>`
 * 示例:`ushoot(1).target(114,514)` → `ucontrol target 114 514 1 0 0`
 * 反编译:`ucontrol ` target/targetp → `ushoot(<shoot>)` + `.target(...)`
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        var H = Builtins.Helpers;
        var R = Builtins.Registry;
        var CHAIN = [
            { key: 'main', def: '1' },
            { key: 'target', def: '@this' }
        ];
        Ctrl.ushoot = {
            key: 'ushoot',
            mcode: 'ucontrol',
            mcodeSelect: ['target', 'targetp'],
            chain: CHAIN,
            compile: function (s, ctx) {
                var m = ctx.chain(s);
                var shoot = H.chainGet(m, 'main', R.chainDef(CHAIN, 'main'));
                var tgt = H.chainGet(m, 'target', R.chainDef(CHAIN, 'target'));
                var kind = tgt.indexOf(',') !== -1 ? 'target' : 'targetp';
                return 'ucontrol ' + kind + ' ' + H.padZero(5, [tgt.replace(/,/g, ' '), shoot]);
            },
            restore: function (s) {
                var parts = H.splitInto(s, 2);
                var ps = parts[1].split(' ');
                var target;
                var target2 = '';
                if (parts[0] === 'targetp') {
                    target = ps[1] == null ? '' : ps[1];
                    if (target === '1')
                        target = '';
                    target2 = '.target(' + (ps[0] == null ? '' : ps[0]) + ')';
                }
                else {
                    target = ps[2] == null ? '' : ps[2];
                    if (target === '1')
                        target = '';
                    target2 = '.target(' + [ps[0] == null ? '' : ps[0], ps[1] == null ? '' : ps[1]].join(',') + ')';
                }
                return 'ushoot(' + target + ')' + target2;
            }
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * wait — 透传指令。
 *
 * 语法:`wait(<秒数>)`
 * 输出:`wait <s>`
 * 反编译:`wait ` → `wait(<s>)`
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        Ctrl.wait = {
            key: 'wait',
            compile: function (s) { return 'wait ' + s; },
            restore: function (s) { return 'wait(' + s + ')'; }
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * Ctrl 大类注册(顺序 = 匹配顺序;raw 必须最后,否则吞掉 draw( 等)。
 * 匹配符号由大类定义:裸指令名 + "("。
 */
var Builtins;
(function (Builtins) {
    var Ctrl;
    (function (Ctrl) {
        Ctrl.category = {
            keyOf: function (name) { return name + '('; },
            defs: [
                Ctrl.print, Ctrl.printchar, Ctrl.format, Ctrl.wait, Ctrl.stop, Ctrl.end,
                Ctrl.ubind, Ctrl.uctrl, Ctrl.ushoot, Ctrl.draw, Ctrl.jump, Ctrl.jump2, Ctrl.printf, Ctrl.tag, Ctrl.raw
            ]
        };
    })(Ctrl = Builtins.Ctrl || (Builtins.Ctrl = {}));
})(Builtins || (Builtins = {}));
/**
 * .color — 控制指令;mcode 为共享指令字 `control`,按 color 分派。
 *
 * 语法:`<block>.color(<r>,<g>,<b>,<a>)`
 * 输出:`control color <block> <pad(4, s)>`
 * 反编译:`control color ` → `<block>.color(<r,g,b,a>)`
 */
var Builtins;
(function (Builtins) {
    var DotCtrl;
    (function (DotCtrl) {
        var H = Builtins.Helpers;
        DotCtrl.color = {
            key: 'color',
            mcode: 'control',
            mcodeSelect: ['color'],
            compile: function (s, ctx) { return 'control color ' + ctx.block() + ' ' + H.padZero(4, ctx.parts(s)); },
            restore: function (s) {
                var parts = H.splitInto(s, 3);
                var ps = (parts[2] == null ? '' : parts[2]).split(' ');
                return parts[1] + '.color(' + (ps[0] == null ? '' : ps[0]) + ')';
            }
        };
    })(DotCtrl = Builtins.DotCtrl || (Builtins.DotCtrl = {}));
})(Builtins || (Builtins = {}));
/**
 * .config — 控制指令;mcode 为共享指令字 `control`,按 config 分派。
 *
 * 语法:`<block>.config(<值>)`
 * 输出:`control config <block> <pad(4, s)>`
 * 反编译:`control config ` → `<block>.config(<值>)`
 */
var Builtins;
(function (Builtins) {
    var DotCtrl;
    (function (DotCtrl) {
        var H = Builtins.Helpers;
        DotCtrl.config = {
            key: 'config',
            mcode: 'control',
            mcodeSelect: ['config'],
            compile: function (s, ctx) { return 'control config ' + ctx.block() + ' ' + H.padZero(4, ctx.parts(s)); },
            restore: function (s) {
                var parts = H.splitInto(s, 3);
                var ps = (parts[2] == null ? '' : parts[2]).split(' ');
                return parts[1] + '.config(' + (ps[0] == null ? '' : ps[0]) + ')';
            }
        };
    })(DotCtrl = Builtins.DotCtrl || (Builtins.DotCtrl = {}));
})(Builtins || (Builtins = {}));
/**
 * .ctrl — 控制指令兜底;mcode 为共享指令字 `control`(无 mcodeSelect = 兜底)。
 *
 * 语法:`<block>.ctrl(<类型>,<参数…>)`
 * 输出:`control <w0|enabled> <block> <pad(4, w1)>`(w0 缺省 enabled,w1 缺省空串)
 * 反编译:`control ` 未知类型 → `<block>.ctrl(reduce(0, 类型 参数…))`
 */
var Builtins;
(function (Builtins) {
    var DotCtrl;
    (function (DotCtrl) {
        var H = Builtins.Helpers;
        var D = Builtins.Domain;
        DotCtrl.ctrl = {
            key: 'ctrl',
            mcode: 'control',
            compile: function (s, ctx) {
                var w = ctx.parts(s);
                var type = H.getOr(w, 0, D.CONTROL_DEFAULT);
                return 'control' + type + ctx.block() + ' ' + H.padZero(4, [H.getOr(w, 1, '')]);
            },
            restore: function (s) {
                var parts = H.splitInto(s, 3);
                return parts[1] + '.ctrl(' + H.reduce('0', parts[0] + ' ' + (parts[2] == null ? '' : parts[2])) + ')';
            }
        };
    })(DotCtrl = Builtins.DotCtrl || (Builtins.DotCtrl = {}));
})(Builtins || (Builtins = {}));
/**
 * .dflush — 绘制冲刷指令。
 *
 * 语法:`<block>.dflush()`
 * 输出:`drawflush <block>`
 * 反编译:`drawflush ` → `<block>.dflush()`
 */
var Builtins;
(function (Builtins) {
    var DotCtrl;
    (function (DotCtrl) {
        DotCtrl.dflush = {
            key: 'dflush',
            mcode: 'drawflush',
            compile: function (_s, ctx) { return 'drawflush ' + ctx.block(); },
            restore: function (s) { return s + '.dflush()'; }
        };
    })(DotCtrl = Builtins.DotCtrl || (Builtins.DotCtrl = {}));
})(Builtins || (Builtins = {}));
/**
 * .enable — 控制指令;mcode 为共享指令字 `control`,按 enabled 分派。
 *
 * 语法:`<block>.enable(<0|1>)`
 * 输出:`control enabled <block> <pad(4, s)>`
 * 反编译:`control enabled ` → `<block>.enable(<0|1>)`
 */
var Builtins;
(function (Builtins) {
    var DotCtrl;
    (function (DotCtrl) {
        var H = Builtins.Helpers;
        DotCtrl.enable = {
            key: 'enable',
            mcode: 'control',
            mcodeSelect: ['enabled'],
            compile: function (s, ctx) { return 'control enabled ' + ctx.block() + ' ' + H.padZero(4, ctx.parts(s)); },
            restore: function (s) {
                var parts = H.splitInto(s, 3);
                var ps = (parts[2] == null ? '' : parts[2]).split(' ');
                return parts[1] + '.enable(' + (ps[0] == null ? '' : ps[0]) + ')';
            }
        };
    })(DotCtrl = Builtins.DotCtrl || (Builtins.DotCtrl = {}));
})(Builtins || (Builtins = {}));
/**
 * .pflush — 打印冲刷指令。
 *
 * 语法:`<block>.pflush()`
 * 输出:`printflush <block>`
 * 反编译:`printflush ` → `<block>.pflush()`
 */
var Builtins;
(function (Builtins) {
    var DotCtrl;
    (function (DotCtrl) {
        DotCtrl.pflush = {
            key: 'pflush',
            mcode: 'printflush',
            compile: function (_s, ctx) { return 'printflush ' + ctx.block(); },
            restore: function (s) { return s + '.pflush()'; }
        };
    })(DotCtrl = Builtins.DotCtrl || (Builtins.DotCtrl = {}));
})(Builtins || (Builtins = {}));
/**
 * .shoot — 链式参数指令;mcode 为共享指令字 `control`,按 shoot/shootp 分派。
 *
 * 语法:`<block>.shoot(<shoot>).target(<设计目标>|<x>,<y>)`;shoot 缺省 `1`,target 缺省 `@this`
 * 链式键:main(射击开关), target(设计目标或坐标)
 * 说明:括号内只有射击开关;x/y 坐标或设计目标经 .target(...) 传递,
 *   由 target 参数量区分——双参为 xy 坐标(shoot),单参为设计目标(shootp)
 * 输出:`control shoot <block> <pad(4, tgt 逗号替换为空格, shoot)>`(坐标)
 *   或 `control shootp <block> <pad(4, tgt, shoot)>`(设计目标)
 * 示例:`turret.shoot(1).target(5,6)` → `control shoot turret 5 6 1 0`
 * 反编译:`control ` shoot/shootp → `<block>.shoot(<shoot>)` + `.target(...)`
 */
var Builtins;
(function (Builtins) {
    var DotCtrl;
    (function (DotCtrl) {
        var H = Builtins.Helpers;
        var R = Builtins.Registry;
        var CHAIN = [
            { key: 'main', def: '1' },
            { key: 'target', def: '@this' }
        ];
        DotCtrl.shoot = {
            key: 'shoot',
            mcode: 'control',
            mcodeSelect: ['shoot', 'shootp'],
            chain: CHAIN,
            compile: function (s, ctx) {
                var m = ctx.chain(s);
                var shoot = H.chainGet(m, 'main', R.chainDef(CHAIN, 'main'));
                var tgt = H.chainGet(m, 'target', R.chainDef(CHAIN, 'target'));
                var kind = tgt.indexOf(',') !== -1 ? 'shoot' : 'shootp';
                return 'control ' + kind + ' ' + ctx.block() + ' ' + H.padZero(4, [tgt.replace(/,/g, ' '), shoot]);
            },
            restore: function (s) {
                var parts = H.splitInto(s, 3);
                var ps = (parts[2] == null ? '' : parts[2]).split(' ');
                var target;
                var target2 = '';
                if (parts[0] === 'shoot') {
                    target = ps[2] == null ? '' : ps[2];
                    if (target === '1')
                        target = '';
                    target2 = '.target(' + [ps[0] == null ? '' : ps[0], ps[1] == null ? '' : ps[1]].join(',') + ')';
                }
                else {
                    target = ps[1] == null ? '' : ps[1];
                    if (target === '1')
                        target = '';
                    target2 = '.target(' + (ps[0] == null ? '' : ps[0]) + ')';
                }
                return parts[1] + '.shoot(' + target + ')' + target2;
            }
        };
    })(DotCtrl = Builtins.DotCtrl || (Builtins.DotCtrl = {}));
})(Builtins || (Builtins = {}));
/**
 * .ulocate — 链式定位指令。
 *
 * 语法:`<block>.ulocate(<type>).ore(<ore>).building(<bld>).enemy(<enemy>)`
 * 链式键:main(定位类型,缺省 ore), ore(缺省 0), building(缺省 core), enemy(缺省 0)
 * 行为:type 命中 Building 分类(buildingContains)时,building = type 且 type = building
 * 输出:`ulocate <type> <building> <enemy> <ore> <block>.x <block>.y <block>.f <block>`
 * 反编译:`ulocate ` → `<block>.ulocate(<type>)`(type 为 building 时用 building 参数;
 *   追加 .ore(<ore>)(仅当 type 为 ore)、.enemy(<enemy>)(非 0 时)
 */
var Builtins;
(function (Builtins) {
    var DotCtrl;
    (function (DotCtrl) {
        var H = Builtins.Helpers;
        var D = Builtins.Domain;
        var R = Builtins.Registry;
        var CHAIN = [
            { key: 'main', def: 'ore' },
            { key: 'ore', def: '0' },
            { key: 'building', def: 'core' },
            { key: 'enemy', def: '0' }
        ];
        DotCtrl.ulocate = {
            key: 'ulocate',
            chain: CHAIN,
            compile: function (s, ctx) {
                var m = ctx.chain(s);
                var type = H.chainGet(m, 'main', R.chainDef(CHAIN, 'main'));
                var bld = H.chainGet(m, 'building', R.chainDef(CHAIN, 'building'));
                var ore = H.chainGet(m, 'ore', R.chainDef(CHAIN, 'ore'));
                var en = H.chainGet(m, 'enemy', R.chainDef(CHAIN, 'enemy'));
                if (ctx.buildingContains(type)) {
                    bld = type;
                    type = D.LOCATE_TYPES[1];
                }
                var b = ctx.block();
                return 'ulocate ' + type + ' ' + bld + ' ' + en + ' ' + ore + ' ' +
                    b + '.x ' + b + '.y ' + b + '.f ' + b;
            },
            restore: function (s) {
                var p = s.split(' ');
                var locateType = p[0];
                var building = p[1];
                var enemy = p[2];
                var ore = p[3];
                var block = p[7];
                var result = block + '.ulocate(' + (locateType === 'building' ? building : locateType) + ')';
                if (locateType === 'ore')
                    result += '.ore(' + ore + ')';
                if (enemy !== '0')
                    result += '.enemy(' + enemy + ')';
                return result;
            }
        };
    })(DotCtrl = Builtins.DotCtrl || (Builtins.DotCtrl = {}));
})(Builtins || (Builtins = {}));
/**
 * .unpack — 取色指令。
 *
 * 语法:`<block>.unpack(<r>,<g>,<b>,<a>)`
 * 输出:`unpackcolor <pad(4, s)> <block>`
 * 反编译:`unpackcolor ` → `<block>.unpack(reduce(0, 前4参))`
 */
var Builtins;
(function (Builtins) {
    var DotCtrl;
    (function (DotCtrl) {
        var H = Builtins.Helpers;
        DotCtrl.unpack = {
            key: 'unpack',
            mcode: 'unpackcolor',
            compile: function (s, ctx) { return 'unpackcolor ' + H.padZero(4, ctx.parts(s)) + ' ' + ctx.block(); },
            restore: function (s) {
                var p = s.split(' ');
                return p[4] + '.unpack(' + H.reduceArr('0', 4, p.slice(0, 4)) + ')';
            }
        };
    })(DotCtrl = Builtins.DotCtrl || (Builtins.DotCtrl = {}));
})(Builtins || (Builtins = {}));
/**
 * .write — 内存写指令。
 *
 * 语法:`<block>.write(<内容>,<单元号>)`
 * 输出:`write <w0|null> <block> <w1|0>`
 * 反编译:`write ` → `<block>.write(<内容>[,<单元号>])`(单元号为 0 省略)
 */
var Builtins;
(function (Builtins) {
    var DotCtrl;
    (function (DotCtrl) {
        var H = Builtins.Helpers;
        var D = Builtins.Domain;
        DotCtrl.write = {
            key: 'write',
            compile: function (s, ctx) {
                var w = ctx.parts(s);
                return 'write ' + H.getOr(w, 0, D.VAL_NUL) + ' ' + ctx.block() + ' ' + H.getOr(w, 1, D.VAL_0);
            },
            restore: function (s) {
                var p = s.split(' ');
                var content = p[2] === '0' ? p[0] : p[0] + ',' + p[2];
                return p[1] + '.write(' + content + ')';
            }
        };
    })(DotCtrl = Builtins.DotCtrl || (Builtins.DotCtrl = {}));
})(Builtins || (Builtins = {}));
/**
 * DotCtrl 大类注册
 * 匹配符号由大类定义:裸指令名 + "." + "("。
 */
var Builtins;
(function (Builtins) {
    var DotCtrl;
    (function (DotCtrl) {
        DotCtrl.category = {
            keyOf: function (name) { return '.' + name + '('; },
            defs: [DotCtrl.ctrl, DotCtrl.enable, DotCtrl.config, DotCtrl.color, DotCtrl.shoot, DotCtrl.ulocate, DotCtrl.unpack, DotCtrl.pflush, DotCtrl.dflush, DotCtrl.write]
        };
    })(DotCtrl = Builtins.DotCtrl || (Builtins.DotCtrl = {}));
})(Builtins || (Builtins = {}));
/**
 * .orElse — 条件选择。
 *
 * 语法:`<value>.orElse(<后备>).when(<条件>)`;后备缺省 `0`
 * 链式键:main(后备), when(条件)
 * 条件判定(与 jump 相同,单 token 时不特判 always/never):
 * - 多 token:子编译;非空产物 → 条件取产物末行,结合 expr 修正
 * - 单 token:`notEqual <whenExpr> 0`;空:恒真
 * 输出:`select mid.<ref> <reverseCondition(condition)> <block> <target>`
 * 反编译:`select ` → `<结果>=<target>.orElse(<后备>).when(<条件>)`
 */
var Builtins;
(function (Builtins) {
    var Dot;
    (function (Dot) {
        var H = Builtins.Helpers;
        var R = Builtins.Registry;
        var CHAIN = [
            { key: 'main', def: '0' },
            { key: 'when', def: '' }
        ];
        Dot.orElse = {
            key: 'orElse',
            mcode: 'select',
            chain: CHAIN,
            compile: function (s, ctx) {
                var m = ctx.chain(s);
                var target = H.chainGet(m, 'main', R.chainDef(CHAIN, 'main'));
                var whenExpr = H.chainGet(m, 'when', R.chainDef(CHAIN, 'when'));
                var splitList = ctx.split(whenExpr);
                var condition;
                if (splitList.length > 1) {
                    var bc = ctx.compileSub(whenExpr);
                    if (bc.bash.length > 0) {
                        ctx.setRef(bc.stat);
                        condition = H.getCondition(bc.bash[bc.bash.length - 1]);
                        if (condition !== 'always 0 0') {
                            bc.bash = bc.bash.slice(0, bc.bash.length - 1);
                        }
                        else if (bc.expr !== '') {
                            condition = 'notEqual ' + bc.expr + ' 0';
                        }
                        ctx.bashAll(bc.bash);
                    }
                    else {
                        condition = 'always 0 0';
                    }
                }
                else if (splitList.length === 1) {
                    condition = 'notEqual ' + whenExpr + ' 0';
                }
                else {
                    condition = 'always 0 0';
                }
                return 'select ' + ctx.mid() + ' ' + H.reverseCondition(condition) +
                    ' ' + ctx.block() + ' ' + target;
            },
            restore: function (s) {
                var p = s.split(' ');
                var condition = [p[1], p[2], p[3]].join(' ');
                return p[0] + '=' + p[4] + '.orElse(' + p[5] + ')' +
                    '.when(' + H.reduceCondition(H.reverseCondition(condition)) + ')';
            }
        };
    })(Dot = Builtins.Dot || (Builtins.Dot = {}));
})(Builtins || (Builtins = {}));
/**
 * .read — 内存读取(表达式,结果变量在等号左侧)。
 *
 * 语法:`<结果> = <block>.read(<单元号>)`
 * 输出:`read mid.<ref> <block> <s>`;调用方随后把 mid.<ref> 代入表达式并递增 ref
 * 反编译:`read ` → `<结果>=<block>.read(<单元号>)`
 */
var Builtins;
(function (Builtins) {
    var Dot;
    (function (Dot) {
        Dot.read = {
            key: 'read',
            mcode: 'read',
            compile: function (s, ctx) { return 'read ' + ctx.mid() + ' ' + ctx.block() + ' ' + s; },
            restore: function (s) {
                var p = s.split(' ');
                return p[0] + '=' + p[1] + '.read(' + p[2] + ')';
            }
        };
    })(Dot = Builtins.Dot || (Builtins.Dot = {}));
})(Builtins || (Builtins = {}));
/**
 * .sensor — 传感器读取(表达式,结果变量在等号左侧)。
 *
 * 语法:`<结果> = <block>.sensor(<属性>)`
 * 输出:`sensor mid.<ref> <block> <s>`;调用方随后把 mid.<ref> 代入表达式并递增 ref
 * 示例:`heat = reactor.sensor(@heat)`
 * 反编译:`sensor ` → `<结果>=<block>.sensor(<属性>)`
 */
var Builtins;
(function (Builtins) {
    var Dot;
    (function (Dot) {
        Dot.sensor = {
            key: 'sensor',
            mcode: 'sensor',
            compile: function (s, ctx) { return 'sensor ' + ctx.mid() + ' ' + ctx.block() + ' ' + s; },
            restore: function (s) {
                var p = s.split(' ');
                return p[0] + '=' + p[1] + '.sensor(' + p[2] + ')';
            }
        };
    })(Dot = Builtins.Dot || (Builtins.Dot = {}));
})(Builtins || (Builtins = {}));
/**
 * Dot 大类注册
 * 匹配符号由大类定义:裸指令名 + "." + "("。
 */
var Builtins;
(function (Builtins) {
    var Dot;
    (function (Dot) {
        Dot.category = {
            keyOf: function (name) { return '.' + name + '('; },
            defs: [Dot.sensor, Dot.read, Dot.orElse]
        };
    })(Dot = Builtins.Dot || (Builtins.Dot = {}));
})(Builtins || (Builtins = {}));
/**
 * abs — 一元运算。
 *
 * 语法:`abs(x)`
 * 输出:`op abs mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=abs(<参数>)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.abs = {
            key: 'abs',
            compile: function (s, ctx) { return 'op abs ' + ctx.mid() + ' ' + s + ' 0'; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * acos — 一元运算。
 *
 * 语法:`acos(x)`
 * 输出:`op acos mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=acos(<参数>)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.acos = {
            key: 'acos',
            compile: function (s, ctx) { return 'op acos ' + ctx.mid() + ' ' + s + ' 0'; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * angle — 二元运算。
 *
 * 语法:`angle(a,b)`
 * 输出:`op angle mid.<ref> <w0> <w1>`(w0/w1 为前两个参数)
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=angle(a,b)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        var H = Builtins.Helpers;
        Front.angle = {
            key: 'angle',
            compile: function (s, ctx) {
                var w = ctx.parts(s);
                return 'op angle ' + ctx.mid() + ' ' + H.getOr(w, 0, 'null') + ' ' + H.getOr(w, 1, 'null');
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * angleDiff — 二元运算。
 *
 * 语法:`angleDiff(a,b)`
 * 输出:`op angleDiff mid.<ref> <w0> <w1>`(w0/w1 为前两个参数)
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=angleDiff(a,b)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        var H = Builtins.Helpers;
        Front.angleDiff = {
            key: 'angleDiff',
            compile: function (s, ctx) {
                var w = ctx.parts(s);
                return 'op angleDiff ' + ctx.mid() + ' ' + H.getOr(w, 0, 'null') + ' ' + H.getOr(w, 1, 'null');
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * asin — 一元运算。
 *
 * 语法:`asin(x)`
 * 输出:`op asin mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=asin(<参数>)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.asin = {
            key: 'asin',
            compile: function (s, ctx) { return 'op asin ' + ctx.mid() + ' ' + s + ' 0'; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * atan — 一元运算。
 *
 * 语法:`atan(x)`
 * 输出:`op atan mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=atan(<参数>)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.atan = {
            key: 'atan',
            compile: function (s, ctx) { return 'op atan ' + ctx.mid() + ' ' + s + ' 0'; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * block — 内容查询;mcode 为共享指令字 `lookup`,按 block 分派。
 *
 * 语法:`block(@copper-wall)`
 * 输出:`lookup block mid.<ref> <s>`
 * 反编译:`lookup block ` → `<结果>=block(<索引>)`
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.block = {
            key: 'block',
            mcode: 'lookup',
            mcodeSelect: ['block'],
            compile: function (s, ctx) { return 'lookup block ' + ctx.mid() + ' ' + s; },
            restore: function (s) {
                var p = s.split(' ');
                return p[1] + '=block(' + p[2] + ')';
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * ceil — 一元运算。
 *
 * 语法:`ceil(x)`
 * 输出:`op ceil mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=ceil(<参数>)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.ceil = {
            key: 'ceil',
            compile: function (s, ctx) { return 'op ceil ' + ctx.mid() + ' ' + s + ' 0'; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * cos — 三角函数(输出无尾随 0)。
 *
 * 语法:`cos(x)`
 * 输出:`op cos mid.<ref> <s>`
 * 反编译:op 行通用还原。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.cos = {
            key: 'cos',
            compile: function (s, ctx) { return 'op cos ' + ctx.mid() + ' ' + s; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * floor — 一元运算。
 *
 * 语法:`floor(x)`
 * 输出:`op floor mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=floor(<参数>)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.floor = {
            key: 'floor',
            compile: function (s, ctx) { return 'op floor ' + ctx.mid() + ' ' + s + ' 0'; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * item — 内容查询;mcode 为共享指令字 `lookup`,按 item 分派。
 *
 * 语法:`item(@copper-wall)`
 * 输出:`lookup item mid.<ref> <s>`
 * 反编译:`lookup item ` → `<结果>=item(<索引>)`
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.item = {
            key: 'item',
            mcode: 'lookup',
            mcodeSelect: ['item'],
            compile: function (s, ctx) { return 'lookup item ' + ctx.mid() + ' ' + s; },
            restore: function (s) {
                var p = s.split(' ');
                return p[1] + '=item(' + p[2] + ')';
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * lb — 以 2 为底对数(op 名 logn,尾参 2)。
 *
 * 语法:`lb(x)`
 * 输出:`op logn mid.<ref> <s> 2`
 * 反编译:op logn 且末参为 2 → `<结果>=lb(<参2>)`。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.lb = {
            key: 'lb',
            compile: function (s, ctx) { return 'op logn ' + ctx.mid() + ' ' + s + ' 2'; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * len — 二元运算。
 *
 * 语法:`len(a,b)`
 * 输出:`op len mid.<ref> <w0> <w1>`(w0/w1 为前两个参数)
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=len(a,b)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        var H = Builtins.Helpers;
        Front.len = {
            key: 'len',
            compile: function (s, ctx) {
                var w = ctx.parts(s);
                return 'op len ' + ctx.mid() + ' ' + H.getOr(w, 0, 'null') + ' ' + H.getOr(w, 1, 'null');
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * lg — 常用对数(op 名 log10)。
 *
 * 语法:`lg(x)`
 * 输出:`op log10 mid.<ref> <s> 0`
 * 反编译:op 行通用还原,log10 按别名表还原为 lg(<参数>)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.lg = {
            key: 'lg',
            compile: function (s, ctx) { return 'op log10 ' + ctx.mid() + ' ' + s + ' 0'; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * link — 方块链接引用。
 *
 * 语法:`link(<索引>)`
 * 输出:`getlink mid.<ref> <s>`
 * 反编译:`getlink ` → `<结果>=link(<索引>)`
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.link = {
            key: 'link',
            mcode: 'getlink',
            compile: function (s, ctx) { return 'getlink ' + ctx.mid() + ' ' + s; },
            restore: function (s) {
                var p = s.split(' ');
                return p[0] + '=link(' + p[1] + ')';
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * liquid — 内容查询;mcode 为共享指令字 `lookup`,按 liquid 分派。
 *
 * 语法:`liquid(@copper-wall)`
 * 输出:`lookup liquid mid.<ref> <s>`
 * 反编译:`lookup liquid ` → `<结果>=liquid(<索引>)`
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.liquid = {
            key: 'liquid',
            mcode: 'lookup',
            mcodeSelect: ['liquid'],
            compile: function (s, ctx) { return 'lookup liquid ' + ctx.mid() + ' ' + s; },
            restore: function (s) {
                var p = s.split(' ');
                return p[1] + '=liquid(' + p[2] + ')';
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * ln — 自然对数(op 名 log)。
 *
 * 语法:`ln(x)`
 * 输出:`op log mid.<ref> <s> 0`
 * 反编译:op 行通用还原,log 按别名表还原为 ln(<参数>)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.ln = {
            key: 'ln',
            compile: function (s, ctx) { return 'op log ' + ctx.mid() + ' ' + s + ' 0'; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * log — 任意底对数(两参交换)。
 *
 * 语法:`log(<底数>,<真数>)`
 * 输出:`op logn mid.<ref> <w1> <w0>`(两参交换)
 * 示例:`log(2,8)` → `op logn mid.1 8 2`
 * 反编译:op 行通用还原(log 交换两参并去尾部 0)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        var H = Builtins.Helpers;
        Front.log = {
            key: 'log',
            compile: function (s, ctx) {
                var w = ctx.parts(s);
                return 'op logn ' + ctx.mid() + ' ' + H.getOr(w, 1, 'null') + ' ' + H.getOr(w, 0, 'null');
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * lookup — 通用内容查询;mcode 为共享指令字 `lookup`(兜底:非标准内容类型)。
 *
 * 语法:`lookup(<类型>,<索引>)`
 * 输出:`lookup <w0|block> mid.<ref> <wLast|0>`(类型缺省 block,索引取末参缺省 0)
 * 反编译:`lookup ` 非标准类型 → `<结果>=lookup(<类型>,<索引>)`
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        var H = Builtins.Helpers;
        var D = Builtins.Domain;
        Front.lookup = {
            key: 'lookup',
            compile: function (s, ctx) {
                var w = ctx.parts(s);
                return 'lookup ' + H.getOr(w, 0, D.LOOKUP_TYPES[0]) + ' ' + ctx.mid() + ' ' + H.getLastOr(w, D.VAL_0);
            },
            restore: function (s) {
                var p = s.split(' ');
                return p[1] + '=lookup(' + p[0] + ',' + p[2] + ')';
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * max — 二元运算。
 *
 * 语法:`max(a,b)`
 * 输出:`op max mid.<ref> <w0> <w1>`(w0/w1 为前两个参数)
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=max(a,b)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        var H = Builtins.Helpers;
        Front.max = {
            key: 'max',
            compile: function (s, ctx) {
                var w = ctx.parts(s);
                return 'op max ' + ctx.mid() + ' ' + H.getOr(w, 0, 'null') + ' ' + H.getOr(w, 1, 'null');
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * min — 二元运算。
 *
 * 语法:`min(a,b)`
 * 输出:`op min mid.<ref> <w0> <w1>`(w0/w1 为前两个参数)
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=min(a,b)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        var H = Builtins.Helpers;
        Front.min = {
            key: 'min',
            compile: function (s, ctx) {
                var w = ctx.parts(s);
                return 'op min ' + ctx.mid() + ' ' + H.getOr(w, 0, 'null') + ' ' + H.getOr(w, 1, 'null');
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * noise — 二元运算。
 *
 * 语法:`noise(a,b)`
 * 输出:`op noise mid.<ref> <w0> <w1>`(w0/w1 为前两个参数)
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=noise(a,b)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        var H = Builtins.Helpers;
        Front.noise = {
            key: 'noise',
            compile: function (s, ctx) {
                var w = ctx.parts(s);
                return 'op noise ' + ctx.mid() + ' ' + H.getOr(w, 0, 'null') + ' ' + H.getOr(w, 1, 'null');
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * not — 一元运算。
 *
 * 语法:`not(x)`
 * 输出:`op not mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=not(<参数>)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.not = {
            key: 'not',
            compile: function (s, ctx) { return 'op not ' + ctx.mid() + ' ' + s + ' 0'; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * pack — 颜色打包。
 *
 * 语法:`pack(<r>,<g>,<b>,<a>)`
 * 输出:`packcolor mid.<ref> <pad(4, s)>`
 * 反编译:`packcolor ` → `<结果>=pack(<r,g,b,a>)`
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        var H = Builtins.Helpers;
        Front.pack = {
            key: 'pack',
            mcode: 'packcolor',
            compile: function (s, ctx) { return 'packcolor ' + ctx.mid() + ' ' + H.padZero(4, ctx.parts(s)); },
            restore: function (s) {
                var idx = s.indexOf(' ');
                var var0 = idx === -1 ? s : s.slice(0, idx);
                var rest = idx === -1 ? '' : s.slice(idx + 1);
                return var0 + '=pack(' + rest.replace(/ /g, ',') + ')';
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * radar — 方块雷达(链式参数)。
 *
 * 语法:`radar().target(<t>).sort(<s>).main(<敌方目标>).order(<o>)`
 * 链式键:target(缺省 enemy,any,any), sort(缺省 distance), main(缺省 @this), order(缺省 1)
 * 输出:`radar <pad(any, 3, target)> <sort> <main> <order> mid.<ref>`
 * 反编译:`radar ` → `<结果>=uradar(<主体>)` + .target/.order/.sort 链(@this 主体省略)
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        var H = Builtins.Helpers;
        var R = Builtins.Registry;
        var CHAIN = [
            { key: 'target', def: 'enemy,any,any' },
            { key: 'sort', def: 'distance' },
            { key: 'main', def: '@this' },
            { key: 'order', def: '1' }
        ];
        Front.radar = {
            key: 'radar',
            mcode: 'radar',
            chain: CHAIN,
            compile: function (s, ctx) {
                var m = ctx.chain(s);
                var target = H.pad('any', 3, ctx.parts(H.chainGet(m, 'target', R.chainDef(CHAIN, 'target'))));
                var sort = H.chainGet(m, 'sort', R.chainDef(CHAIN, 'sort'));
                var main = H.chainGet(m, 'main', R.chainDef(CHAIN, 'main'));
                var order = H.chainGet(m, 'order', R.chainDef(CHAIN, 'order'));
                return 'radar ' + target + ' ' + sort + ' ' + main + ' ' + order + ' ' + ctx.mid();
            },
            restore: function (s) {
                var p = s.split(' ');
                var order = p[5];
                var sort = p[3];
                var block = p[4];
                var result = p[6] + '=uradar(' + (block === '@this' ? '' : block) + ')';
                var target = H.reduce('any', [p[0], p[1], p[2]].join(' '));
                if (target !== '' && target !== 'enemy')
                    result += '.target(' + target + ')';
                if (order !== '1')
                    result += '.order(' + order + ')';
                if (sort !== 'distance')
                    result += '.sort(' + sort + ')';
                return result;
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * rand — 一元运算。
 *
 * 语法:`rand(x)`
 * 输出:`op rand mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=rand(<参数>)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.rand = {
            key: 'rand',
            compile: function (s, ctx) { return 'op rand ' + ctx.mid() + ' ' + s + ' 0'; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * round — 一元运算。
 *
 * 语法:`round(x)`
 * 输出:`op round mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=round(<参数>)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.round = {
            key: 'round',
            compile: function (s, ctx) { return 'op round ' + ctx.mid() + ' ' + s + ' 0'; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * sign — 一元运算。
 *
 * 语法:`sign(x)`
 * 输出:`op sign mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=sign(<参数>)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.sign = {
            key: 'sign',
            compile: function (s, ctx) { return 'op sign ' + ctx.mid() + ' ' + s + ' 0'; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * sin — 三角函数(输出无尾随 0)。
 *
 * 语法:`sin(x)`
 * 输出:`op sin mid.<ref> <s>`
 * 反编译:op 行通用还原(如 `<结果>=sin(<参数>)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.sin = {
            key: 'sin',
            compile: function (s, ctx) { return 'op sin ' + ctx.mid() + ' ' + s; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * sqrt — 一元运算。
 *
 * 语法:`sqrt(x)`
 * 输出:`op sqrt mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=sqrt(<参数>)`)。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.sqrt = {
            key: 'sqrt',
            compile: function (s, ctx) { return 'op sqrt ' + ctx.mid() + ' ' + s + ' 0'; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * tan — 三角函数(输出无尾随 0)。
 *
 * 语法:`tan(x)`
 * 输出:`op tan mid.<ref> <s>`
 * 反编译:op 行通用还原。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.tan = {
            key: 'tan',
            compile: function (s, ctx) { return 'op tan ' + ctx.mid() + ' ' + s; }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * team — 内容查询;mcode 为共享指令字 `lookup`,按 team 分派。
 *
 * 语法:`team(@copper-wall)`
 * 输出:`lookup team mid.<ref> <s>`
 * 反编译:`lookup team ` → `<结果>=team(<索引>)`
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.team = {
            key: 'team',
            mcode: 'lookup',
            mcodeSelect: ['team'],
            compile: function (s, ctx) { return 'lookup team ' + ctx.mid() + ' ' + s; },
            restore: function (s) {
                var p = s.split(' ');
                return p[1] + '=team(' + p[2] + ')';
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * unit — 内容查询;mcode 为共享指令字 `lookup`,按 unit 分派。
 *
 * 语法:`unit(@copper-wall)`
 * 输出:`lookup unit mid.<ref> <s>`
 * 反编译:`lookup unit ` → `<结果>=unit(<索引>)`
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.unit = {
            key: 'unit',
            mcode: 'lookup',
            mcodeSelect: ['unit'],
            compile: function (s, ctx) { return 'lookup unit ' + ctx.mid() + ' ' + s; },
            restore: function (s) {
                var p = s.split(' ');
                return p[1] + '=unit(' + p[2] + ')';
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * uradar — 单位雷达(链式参数)。
 *
 * 语法:`uradar().target(<t>).sort(<s>).order(<o>)`
 * 链式键:target(缺省 enemy,any,any), sort(缺省 distance), order(缺省 1)
 * 输出:`uradar <pad(any, 3, target)> <sort> 0 <order> mid.<ref>`
 *   (target 按逗号切分填充到 3 项,缺省 any)
 * 反编译:`uradar ` → `<结果>=uradar()` + .target/.order/.sort 链(非缺省时)
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        var H = Builtins.Helpers;
        var R = Builtins.Registry;
        var CHAIN = [
            { key: 'target', def: 'enemy,any,any' },
            { key: 'sort', def: 'distance' },
            { key: 'order', def: '1' }
        ];
        Front.uradar = {
            key: 'uradar',
            mcode: 'uradar',
            chain: CHAIN,
            compile: function (s, ctx) {
                var m = ctx.chain(s);
                var target = H.pad('any', 3, ctx.parts(H.chainGet(m, 'target', R.chainDef(CHAIN, 'target'))));
                var sort = H.chainGet(m, 'sort', R.chainDef(CHAIN, 'sort'));
                var order = H.chainGet(m, 'order', R.chainDef(CHAIN, 'order'));
                return 'uradar ' + target + ' ' + sort + ' 0 ' + order + ' ' + ctx.mid();
            },
            restore: function (s) {
                var p = s.split(' ');
                var order = p[5];
                var sort = p[3];
                var result = p[6] + '=uradar()';
                var target = H.reduce('any', [p[0], p[1], p[2]].join(' '));
                if (target !== '' && target !== 'enemy')
                    result += '.target(' + target + ')';
                if (order !== '1')
                    result += '.order(' + order + ')';
                if (sort !== 'distance')
                    result += '.sort(' + sort + ')';
                return result;
            }
        };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * Front 大类注册(high 组先于 low 组处理)
 * 匹配符号由大类定义:裸指令名 + "("。
 */
var Builtins;
(function (Builtins) {
    var Front;
    (function (Front) {
        Front.keyOf = function (name) { return name + '('; };
        Front.highDefs = [
            Front.not, Front.abs, Front.sign, Front.floor, Front.ceil, Front.round, Front.sqrt, Front.rand, Front.asin, Front.acos, Front.atan,
            Front.ln, Front.lg, Front.lb,
            Front.max, Front.min, Front.len, Front.angle, Front.angleDiff, Front.noise, Front.log,
            Front.link, Front.lookup, Front.block, Front.unit, Front.item, Front.liquid, Front.team, Front.pack, Front.uradar
        ];
        Front.lowDefs = [Front.sin, Front.cos, Front.tan, Front.radar];
        Front.defs = Front.highDefs.concat(Front.lowDefs);
        Front.high = { keyOf: Front.keyOf, defs: Front.highDefs };
        Front.low = { keyOf: Front.keyOf, defs: Front.lowDefs };
        Front.category = { keyOf: Front.keyOf, defs: Front.defs };
    })(Front = Builtins.Front || (Builtins.Front = {}));
})(Builtins || (Builtins = {}));
/**
 * 注册表组装:由各指令的 InstrDef 双向映射派生 compile / decompile / chain 注册表。
 * 完整扫描键由大类 CategorySpec.keyOf 模板生成(附加匹配符号由大类定义)。
 * 键结构保持与 Java BuiltinEngine 兼容(compile 五类映射 + decompile + chain + 数据)。
 */
var Builtins;
(function (Builtins) {
    var ALL_DEFS = [].concat(Builtins.Ctrl.category.defs, Builtins.DotCtrl.category.defs, Builtins.Dot.category.defs, Builtins.Front.category.defs);
    function buildRegistry() {
        var decompile = Builtins.Registry.decompileMap(ALL_DEFS);
        // 语言机制级通用还原(非内置指令):赋值与运算符行
        decompile['set '] = Builtins.Registry.setRestore;
        decompile['op '] = Builtins.Registry.opRestore();
        // chain 表:各大类的链式指令声明(完整扫描键 → 合法链键)
        var chain = {};
        for (var _i = 0, _a = [Builtins.Ctrl.category, Builtins.DotCtrl.category, Builtins.Dot.category, Builtins.Front.category]; _i < _a.length; _i++) {
            var spec = _a[_i];
            var t = Builtins.Registry.chainTable(spec);
            for (var _b = 0, _c = Object.keys(t); _b < _c.length; _b++) {
                var k = _c[_b];
                chain[k] = t[k];
            }
        }
        return {
            ctrl: Builtins.Registry.compileMap(Builtins.Ctrl.category),
            dotCtrl: Builtins.Registry.compileMap(Builtins.DotCtrl.category),
            dot: Builtins.Registry.compileMap(Builtins.Dot.category),
            frontHigh: Builtins.Registry.compileMap(Builtins.Front.high),
            frontLow: Builtins.Registry.compileMap(Builtins.Front.low),
            decompile: decompile,
            chain: chain,
            domain: Builtins.Domain,
            codes: Builtins.Codes,
            operators: Builtins.Operators
        };
    }
    Builtins.registry = buildRegistry();
})(Builtins || (Builtins = {}));

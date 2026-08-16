#!/usr/bin/env python3
"""render_readme.py — render per-locale READMEs from nested JSON docs.

Inputs:
    assets/docs/*.json           README content per locale (nested schema:
                                 headings / descriptions / features / archTree ...)
    assets/templates/README.md   README template with {{dot.path}} tokens

Outputs:
    README.md                    root view of root_lang
    docs/{code}/README.md        docs view for every locale (en included)

Env:
    ROOT_LANG   root README language (default: zh)
"""
import glob
import json
import os
import sys


def load_json(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def read_text(path):
    with open(path, encoding="utf-8") as f:
        return f.read()


def write_text(path, text):
    parent = os.path.dirname(path)
    if parent:
        os.makedirs(parent, exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)


def collect_tokens(obj, prefix=""):
    """Flatten nested docs into {dot.path: value}; dicts nest, lists become
    indexed (features.feat1 -> features.feat1.0, features.feat1.1, ...).
    Keys starting with '_' are comments and are skipped."""
    tokens = {}
    for k, v in obj.items():
        if k.startswith("_"):
            continue
        path = f"{prefix}.{k}" if prefix else k
        if isinstance(v, dict):
            tokens.update(collect_tokens(v, path))
        elif isinstance(v, list):
            for i, item in enumerate(v):
                tokens[f"{path}.{i}"] = str(item)
        else:
            tokens[path] = str(v)
    return tokens


def main():
    root_lang = os.environ.get("ROOT_LANG", "zh")
    # reverse-alphabetical file order (sort -r): zh.json, zh-Hant.json, en.json
    paths = sorted(glob.glob("assets/docs/*.json"), reverse=True)
    docs = {}
    for p in paths:
        stem = os.path.splitext(os.path.basename(p))[0]
        docs[stem] = load_json(p)
    if not docs:
        print("No assets/docs/*.json — nothing to render.")
        return 1

    stems = [os.path.splitext(os.path.basename(p))[0] for p in paths]

    label_counts = {}
    for stem in docs:
        label = docs[stem].get("lang", stem)
        label_counts[label] = label_counts.get(label, 0) + 1

    def render_languages(cur_code, doc_prefix):
        items = []
        for stem in stems:
            label = docs[stem].get("lang", stem)
            region = docs[stem].get("langRegion", "")
            # shared label (e.g. 中文 in zh + zh-Hant) gets a region suffix
            if label_counts.get(label, 0) > 1:
                label = f"{label} ({region})"
            if stem == cur_code:
                items.append(label)
            else:
                items.append(f'<a href="{doc_prefix}{stem}/README.md">{label}</a>')
        return " &nbsp;|&nbsp; ".join(items)

    template = read_text("assets/templates/README.md")

    for stem, doc in docs.items():
        outputs = [f"docs/{stem}/README.md"]
        if stem == root_lang:
            outputs.append("README.md")

        for out in outputs:
            root_view = (out == "README.md")
            doc_prefix = "docs/" if root_view else "../"
            icon_prefix = "" if root_view else "../../"
            # 文档链接前缀:root view 指向 root_lang 语言目录;docs view 指向同目录
            docs_prefix = f"docs/{root_lang}/" if root_view else ""
            # docs 根路径(root view: docs/;docs view: 上级 ../)
            docs_root = "docs/" if root_view else "../"
            docs_instr = "docs/instructions/README.md" if root_view else "../instructions/README.md"

            tpl = template
            tpl = tpl.replace("{{icon_prefix}}", icon_prefix)
            tpl = tpl.replace("{{languages}}",
                              render_languages(stem, doc_prefix))

            for token, value in collect_tokens(doc).items():
                tpl = tpl.replace("{{" + token + "}}", value)

            # 语言化链接 token(在 json token 代入之后处理,避免遗漏 json 值内的引用)
            tpl = tpl.replace("{{docs_prefix}}", docs_prefix)
            tpl = tpl.replace("{{docs_root}}", docs_root)
            tpl = tpl.replace("{{docs_instr}}", docs_instr)

            # docs view: body doc links become ../...
            if not root_view:
                tpl = tpl.replace("](docs/", "](../")

            write_text(out, tpl)
            print(f"Rendered {out}")

    return 0


if __name__ == "__main__":
    sys.exit(main())

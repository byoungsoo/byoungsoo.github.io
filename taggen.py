#!/usr/bin/env python3
import os
import re
from pathlib import Path

def extract_tags_from_file(filepath):
    """Extract tags from Jekyll front matter"""
    try:
        with open(filepath, 'r', encoding='utf8') as f:
            content = f.read()
        
        # Match front matter between --- blocks
        match = re.search(r'^---\s*\n(.*?)\n---', content, re.MULTILINE | re.DOTALL)
        if not match:
            return []
        
        front_matter = match.group(1)
        
        # Extract tags line
        tag_match = re.search(r'^tags:\s*(.+)$', front_matter, re.MULTILINE)
        if not tag_match:
            return []
        
        # Parse tags (handle both space-separated and array format)
        tags_str = tag_match.group(1).strip()
        if tags_str.startswith('[') and tags_str.endswith(']'):
            # Array format: [tag1, tag2]
            tags = re.findall(r'[\w-]+', tags_str)
        else:
            # Space-separated format
            tags = tags_str.split()
        
        return [tag.strip('"\',') for tag in tags if tag.strip('"\',')]
    
    except Exception as e:
        print(f"Error reading {filepath}: {e}")
        return []

def create_tag_file(tag_dir, tag):
    """Create a tag file with proper front matter"""
    tag_file = tag_dir / f"{tag}.md"
    
    content = f"""---
layout: tagpage
title: "Tag: {tag}"
tag: {tag}
robots: noindex
---
"""
    
    with open(tag_file, 'w', encoding='utf8') as f:
        f.write(content)
    
    return tag_file

def main():
    # Specific post directories
    post_dirs = [
        '_posts/it/algorithm/',
        '_posts/it/cloud/',
        '_posts/it/k8s/',
        '_posts/it/command/',
        '_posts/it/etc/',
        '_posts/it/network/',
        '_posts/it/programming/',
        '_posts/it/solution/',
        '_posts/it/ml/',
        '_posts/book/dev/',
        '_posts/book/os/',
    ]
    
    post_dirs = [Path(d) for d in post_dirs if Path(d).exists()]
    
    tag_dir = Path('tags')
    tag_dir.mkdir(exist_ok=True)
    
    # Collect all tags
    all_tags = set()
    
    for post_dir in post_dirs:
        print(f"Processing: {post_dir}")
        
        for md_file in post_dir.glob('*.md'):
            tags = extract_tags_from_file(md_file)
            all_tags.update(tags)
    
    # Generate tag files
    created_count = 0
    
    for tag in sorted(all_tags):
        tag_file = tag_dir / f"{tag}.md"
        
        if tag_file.exists():
            print(f"Exists: {tag_file}")
        else:
            create_tag_file(tag_dir, tag)
            created_count += 1
            print(f"Created: {tag_file}")
    
    print(f"\nTotal tags: {len(all_tags)}")
    print(f"Created: {created_count}")

if __name__ == '__main__':
    main()

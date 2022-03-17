import glob
import os

post_directories=[
    '_posts/it/algorithm/',
    '_posts/it/cloud/',
    '_posts/it/command/',
    '_posts/it/etc/',
    '_posts/it/network/',
    '_posts/it/programming/',
    '_posts/it/solution/',
    '_posts/book/it/',
    '_posts/book/fiction/'
    ]

tag_dir = 'tags/'

'''
old_tags = glob.glob(tag_dir + '*.md')
for tag in old_tags:
    os.remove(tag)
'''

for post_dir in post_directories:
    filenames = glob.glob(post_dir + '*md')

    total_tags = []
    for filename in filenames:
        f = open(filename, 'r', encoding='utf8')
        crawl = False
        for line in f:
            if crawl:
                current_tags = line.strip().split()
                if current_tags[0] == 'tags:':
                    total_tags.extend(current_tags[1:])
                    crawl = False
                    break
            if line.strip() == '---':
                if not crawl:
                    crawl = True
                else:
                    crawl = False
                    break
        f.close()
    total_tags = set(total_tags)

    count = 0
    for tag in total_tags:
        tag_filename = tag_dir + tag + '.md'
        exists = os.path.isfile(tag_filename)
        
        if exists:
            print(f'Exists File: {tag_filename}' )
        else:
            count += 1
            f = open(tag_filename, 'a')
            write_str = '---\nlayout: tagpage\ntitle: \"Tag: ' + tag + '\"\ntag: ' + tag + '\nrobots: noindex\n---\n'
            f.write(write_str)
            f.close()

    print("Tags generated, count", count)
    print()

'''
if not os.path.exists(tag_dir):
    os.makedirs(tag_dir)
'''


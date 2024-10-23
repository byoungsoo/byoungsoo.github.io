ps -ef | grep "jekyll serve" | grep -v grep | awk {'print $2'} | xargs kill -15

nohup bundle exec jekyll serve > /dev/null &
#nohup bundle exec jekyll serve > ~/jekyll.log 2>&1 &

ps -ef | grep "jekyll serve" | grep -v grep | awk {'print $2'} | xargs kill -15

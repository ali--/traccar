echo Build the traccar server:
echo apt-get install maven
echo mvn package
echo create the minified web app
echo wget http://cdn.sencha.com/cmd/6.2.1/no-jre/SenchaCmd-6.2.1-linux-amd64.sh.zip
echo tar xzvf SenchaCmd-6.2.1-linux-amd64.sh.zip
echo ./SenchaCmd-6.2.1.29-linux-amd64.sh (needs X11)
echo add sencha to PATH. generate ext framework
echo sencha generate app -ext MyApp ./myApp
echo cp ./MyApp/ext .
echo change traccar-web/tools/minify.sh SDK variable to point to ./ext
echo ./traccar-web/tools/minify.sh

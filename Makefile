build:
	mvn -q compile

run: build
	java -cp target/classes site.tanghaojin.App

clean:
	mvn -q clean

.PHONY: build run clean

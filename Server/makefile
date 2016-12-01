#makefile del progetto di programmazione 3. A.A. 2004/2005

#create file Server.jar
jar:
	javac src/*.java -d ./ -classpath ./
	jar cvfm Server.jar MANIFEST.MF *.class
	rm *.class
	@echo "File Server.jar created!"

server:
	java -cp "Server.jar:.:" Server


compile:
	javac src/*.java -d ./ -classpath ./
	@echo "Bytecode created!"

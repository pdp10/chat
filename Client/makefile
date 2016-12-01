#makefile del progetto di programmazione 3. A.A. 2004/2005

#create file Client.jar Server.jar
jar:
	javac src/*.java -d ./ -classpath ./
	jar cvfm Client.jar MANIFEST.MF *.class
	rm *.class
	@echo "File Client.jar created!"

client:
	java -cp "Client.jar:.:" Client

compile:
	javac src/*.java -d ./ -classpath ./
	@echo "Bytecode created!"



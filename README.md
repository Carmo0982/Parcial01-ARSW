### Escuela Colombiana de Ingeniería

### Arquitecturas de Software - ARSW

## Ejercicio Fórmula BBP - Parcial Practico

**Ejercicio Fórmula BBP**

La fórmula [BBP](https://en.wikipedia.org/wiki/Bailey%E2%80%93Borwein%E2%80%93Plouffe_formula) (Bailey–Borwein–Plouffe formula) es un algoritmo que permite calcular el enésimo dígito de PI en base 16, con la particularidad de no necesitar calcular nos n-1 dígitos anteriores. Esta característica permite convertir el problema de calcular un número masivo de dígitos de PI (en base 16) a uno [vergonzosamente paralelo](https://en.wikipedia.org/wiki/Embarrassingly_parallel). En este repositorio encontrará la implementación, junto con un conjunto de pruebas.

Para este ejercicio se quiere calcular, en el menor tiempo posible, y en una sola máquina (aprovechando las características multi-core de la mismas) al menos el primer millón de dígitos de PI (en base 16). Para esto

1. Cree una clase de tipo Thread que represente el ciclo de vida de un hilo que calcule una parte de los dígitos requeridos.
2. Haga que la función PiDigits.getDigits() reciba como parámetro adicional un valor N, correspondiente al número de hilos entre los que se va a paralelizar la solución. Haga que dicha función espere hasta que los N hilos terminen de resolver el problema para combinar las respuestas y entonces retornar el resultado. Para esto, puede utilizar el método Join() del API de concurrencia de Java.
3. Ajuste la implementación para que cada 5 segundos los hilos se detengan e impriman el número de digitos que han procesado y una vez se presione la tecla enter que los hilos continúen su proceso.

## Solución

### Punto 1

Se creó una clase llamada PiDigitsThread que hereda de Thread. Esta clase representa un hilo (un proceso que se ejecuta al mismo tiempo que otros) cuya tarea es calcular una parte específica de los dígitos de PI.

Al crear el hilo, se le pasan dos datos importantes: desde qué posición debe empezar a calcular y cuántos dígitos le toca calcular. Dentro de la clase está incluida la lógica de la fórmula BBP, lo que permite que cada hilo haga su trabajo de manera independiente, sin necesitar información de otros hilos ni de los dígitos anteriores.

Además, la clase tiene un contador interno que va registrando cuántos dígitos ha calculado, lo cual sirve para saber cómo va avanzando mientras se está ejecutando.

### Punto 2

Se modificó la clase PiDigits agregando un nuevo método getDigits(int start, int count, int N). Este método ahora recibe también el número de hilos (N) que se van a usar para hacer el cálculo.

Lo que hace es tomar la cantidad total de dígitos que se quieren calcular y repartirlos en partes iguales entre los N hilos. Si la división no es exacta, los primeros hilos reciben un dígito extra para que se cubran todos los dígitos y no falte ninguno.

Después de crear los hilos, a cada uno se le asigna desde qué posición debe empezar y cuántos dígitos debe calcular. Luego se inician todos los hilos al mismo tiempo. El método principal espera a que todos terminen usando join(), que hace que el programa se detenga hasta que cada hilo haya terminado su trabajo.

Cuando todos finalizan, se juntan los resultados que calculó cada hilo en un solo arreglo de bytes usando System.arraycopy, y ese arreglo se devuelve como resultado final.

El método original getDigits(int start, int count) se dejó igual, sin cambios.

### Punto 3

Para que los hilos se detengan cada 5 segundos, se agregó un sistema de pausa y continuación dentro de PiDigitsThread usando synchronized, wait() y notifyAll().

Cada hilo, antes de calcular un nuevo dígito, revisa si debe pausarse. Si la respuesta es sí, se queda esperando con wait() usando un objeto compartido (lock). Se queda detenido ahí hasta que alguien le avise que puede seguir.

En el método getDigits que usa N hilos, también se crea un hilo extra llamado controlador. Este hilo es de tipo daemon y se encarga de manejar las pausas. Lo que hace es:

Esperar 5 segundos.
Decirle a todos los hilos de cálculo que se pausen.

Mostrar en consola cuántos dígitos ha calculado cada hilo y el total entre todos.
Esperar a que el usuario presione Enter.

Cuando se presiona Enter, el controlador le dice a todos los hilos que continúen. Para eso llama a resumeThread(), que cambia la variable de pausa a falso y ejecuta notifyAll() para despertar a los hilos que estaban esperando.
Este proceso se repite cada 5 segundos hasta que todos los hilos terminan su parte del trabajo.

--

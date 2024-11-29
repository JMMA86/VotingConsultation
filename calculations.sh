#!/bin/bash

NUM_INSTANCIAS=100
BIG_NUM=10000000000
OUTPUT_FILE="resultados.md"

# Inicializar el archivo de salida
echo "# Resultados de las Instancias" > "$OUTPUT_FILE"
echo "| Instancia | Resultado | Tiempo de Respuesta (s) |" >> "$OUTPUT_FILE"
echo "|-----------|-----------|-------------------------|" >> "$OUTPUT_FILE"

# Iterar sobre el número de instancias
for ((i = 1; i <= NUM_INSTANCIAS; i++)); do
  {
    # Medir el tiempo y capturar el código de salida
    START=$(date +%s.%N) # Obtener tiempo inicial
    echo -e "$i"$'\n'"fib $BIG_NUM"$'\n/exit' | java -jar client/build/libs/client.jar
    EXIT_CODE=$?  # Capturar código de salida
    END=$(date +%s.%N)   # Obtener tiempo final

    # Calcular duración
    DURATION=$(awk "BEGIN {print $END - $START}")

    # Determinar resultado
    if [ $EXIT_CODE -eq 0 ]; then
      RESULT="Éxito"
    else
      RESULT="Error (código $EXIT_CODE)"
    fi

    # Escribir resultados en el archivo
    echo "| $i | $RESULT | $DURATION |" >> "$OUTPUT_FILE"
  } &
done

# Esperar a que todas las instancias terminen
wait

echo "Los resultados se han guardado en $OUTPUT_FILE."

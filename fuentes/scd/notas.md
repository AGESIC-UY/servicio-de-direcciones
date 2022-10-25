# Consultas para revisar errores

## Calles sin Localidad y/o sin Departamento

> SELECT FROM calles WHERE localidad IS NULL OR departamento IS NULL; 

## Table Punto sin registros de una calle que existe en Calles.
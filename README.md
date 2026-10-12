<div align="center">

<img width="100%" src="https://capsule-render.vercel.app/api?type=waving&color=0F172A,1E293B,0EA5E9&height=140&section=header&text=SandSketch+Gyro&fontSize=48&fontColor=38BDF8&animation=fadeIn&fontAlignY=38&desc=inclina+%C2%B7+pinta+%C2%B7+sacude&descAlignY=68&descSize=14&descColor=FBBF24"/>

[![Typing SVG](https://readme-typing-svg.demolab.com?font=Fira+Code&size=13&duration=3500&pause=1500&color=38BDF8&center=true&vCenter=true&width=520&lines=Pizarra+de+arena+con+giroscopio;Giroscopio+%C2%B7+Aceler%C3%B3metro+%C2%B7+Jetpack+Compose;Inclina+para+mover.+Toca+para+pintar.;Sacude+para+borrar.)](https://git.io/typing-svg)

![Kotlin](https://img.shields.io/badge/Kotlin-0F172A?style=flat-square&logo=kotlin&logoColor=38BDF8)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-0F172A?style=flat-square&logo=jetpackcompose&logoColor=38BDF8)
![Android](https://img.shields.io/badge/Android-0F172A?style=flat-square&logo=android&logoColor=38BDF8)

</div>

---

Pizarra de arena para Android hecha con Jetpack Compose. Inclinas el teléfono y el giroscopio mueve un cursor. Si tienes el dedo en la pantalla, el cursor va dejando granos de arena. Si sacudes el teléfono, la arena cae y el lienzo queda limpio.

---

## Cómo se usa

1. Inclina el teléfono para mover el cursor.
2. Deja el dedo apoyado en la pantalla para pintar.
3. Doble toque para volver a centrar el cursor (el giroscopio se va corriendo con el tiempo).
4. Sacude el teléfono para borrar.
5. El botón "Datos y gráficos" abre el panel con los gráficos, el análisis y los ajustes del pincel.

---

## Sensores

| Sensor | Para qué se usa |
| --- | --- |
| Giroscopio (TYPE_GYROSCOPE) | Mover el cursor |
| Acelerómetro (TYPE_ACCELEROMETER) | Detectar la sacudida que borra |

---

## Qué tiene el panel

- Sensores: gráfico de líneas en tiempo real (últimos 5 segundos), dibujado con Canvas.
- Mi meta: gráfico de barras con los puntos de cada dibujo contra la meta que escribe el usuario, dibujado con Canvas.
- Análisis: Top-Down (de lo general al detalle) y Bottom-Up (de un evento a una conclusión).
- Pincel: grosor, velocidad del cursor y color.

---

## Estructura del código

```
com.sv.aimlabgyrotrainer
├── MainActivity.kt             arranque, ciclo de vida de los sensores
├── sensors/SensorSource.kt     capa de hardware (SensorManager)
├── viewmodel/SandViewModel.kt  capa de estado y lógica
├── model/Models.kt             constantes y data classes
└── ui/                         capa de presentación (Compose)
    ├── SandScreen.kt
    ├── SandCanvas.kt
    ├── HudComponents.kt
    ├── MetricsSheet.kt
    ├── Charts.kt
    └── AnalysisModule.kt
```

---

<div align="center">

*Hecho en El Salvador 🇸🇻*

<img width="100%" src="https://capsule-render.vercel.app/api?type=waving&color=0F172A,1E293B,0EA5E9&height=80&section=footer"/>

</div>

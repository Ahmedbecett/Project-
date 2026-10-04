package com.example.data.repository

import com.example.data.model.*

object CourseData {

    val supportedLanguages = listOf(
        SupportedLanguage("es", "Spanish", "Español", "🇪🇸", "es-ES"),
        SupportedLanguage("fr", "French", "Français", "🇫🇷", "fr-FR"),
        SupportedLanguage("de", "German", "Deutsch", "🇩🇪", "de-DE"),
        SupportedLanguage("it", "Italian", "Italiano", "🇮🇹", "it-IT"),
        SupportedLanguage("en", "English", "English", "🇬🇧", "en-US"),
        SupportedLanguage("ar", "Arabic", "العربية", "🇸🇦", "ar-SA"),
        SupportedLanguage("ja", "Japanese", "日本語", "🇯🇵", "ja-JP"),
        SupportedLanguage("pt", "Portuguese", "Português", "🇧🇷", "pt-BR")
    )

    fun getLessonsForLevel(languageId: String, level: CefrLevel): List<Lesson> {
        return when (level) {
            CefrLevel.A1 -> listOf(
                Lesson(
                    id = "${languageId}_a1_1",
                    level = CefrLevel.A1,
                    unitNumber = 1,
                    title = "Greetings & First Introductions",
                    description = "Master essential polite greetings, introducing yourself, and saying farewell.",
                    skillType = SkillType.VOCABULARY,
                    xpReward = 25,
                    exercises = listOf(
                        Exercise(
                            id = "ex_a1_1_1",
                            type = ExerciseType.MULTIPLE_CHOICE,
                            question = "How do you say 'Hello, nice to meet you' in Spanish?",
                            instruction = "Select the correct formal greeting",
                            options = listOf("Hola, mucho gusto", "Adiós, hasta luego", "Por favor, gracias", "Buenas noches"),
                            correctAnswer = "Hola, mucho gusto",
                            audioPhrase = "Hola, mucho gusto",
                            translation = "Hello, nice to meet you",
                            explanation = "'Mucho gusto' expresses pleasure upon meeting someone for the first time."
                        ),
                        Exercise(
                            id = "ex_a1_1_2",
                            type = ExerciseType.LISTENING_CHOICE,
                            question = "Listen and identify what is being said:",
                            instruction = "Tap the speaker icon to listen and select the meaning",
                            options = listOf("My name is Carlos", "I am twenty years old", "Where is the station?", "I speak English"),
                            correctAnswer = "My name is Carlos",
                            audioPhrase = "Me llamo Carlos y soy estudiante",
                            translation = "My name is Carlos and I am a student"
                        ),
                        Exercise(
                            id = "ex_a1_1_3",
                            type = ExerciseType.SENTENCE_BUILDER,
                            question = "Arrange the words to form: 'Good morning, how are you?'",
                            instruction = "Tap words in the correct grammatical order",
                            options = listOf("Buenos", "¿cómo", "días,", "estás?", "noches", "tardes"),
                            correctAnswer = "Buenos días, ¿cómo estás?",
                            audioPhrase = "Buenos días, ¿cómo estás?",
                            translation = "Good morning, how are you?"
                        ),
                        Exercise(
                            id = "ex_a1_1_4",
                            type = ExerciseType.PRONUNCIATION_SPEAK,
                            question = "Pronounce this sentence clearly:",
                            instruction = "Speak into the microphone to verify pronunciation",
                            options = emptyList(),
                            correctAnswer = "Encantado de conocerte",
                            audioPhrase = "Encantado de conocerte",
                            translation = "Delighted to meet you",
                            explanation = "Enunciate every syllable with a gentle trill on the 'r'."
                        )
                    )
                ),
                Lesson(
                    id = "${languageId}_a1_2",
                    level = CefrLevel.A1,
                    unitNumber = 2,
                    title = "Numbers, Time & Daily Hours",
                    description = "Learn counting 1-100, asking for the time, and scheduling appointments.",
                    skillType = SkillType.VOCABULARY,
                    xpReward = 25,
                    exercises = listOf(
                        Exercise(
                            id = "ex_a1_2_1",
                            type = ExerciseType.MULTIPLE_CHOICE,
                            question = "¿Qué hora es? (It is 3:30)",
                            instruction = "Choose the correct time expression",
                            options = listOf("Son las tres y media", "Es la una en punto", "Son las cuatro menos cuarto", "Son las dos y diez"),
                            correctAnswer = "Son las tres y media",
                            audioPhrase = "Son las tres y media",
                            translation = "It is three thirty"
                        ),
                        Exercise(
                            id = "ex_a1_2_2",
                            type = ExerciseType.FILL_IN_BLANK,
                            question = "El tren sale a las _____ de la tarde (seven).",
                            instruction = "Select the missing number",
                            options = listOf("siete", "cinco", "ocho", "seis"),
                            correctAnswer = "siete",
                            audioPhrase = "El tren sale a las siete de la tarde",
                            translation = "The train leaves at seven in the afternoon"
                        )
                    )
                ),
                Lesson(
                    id = "${languageId}_a1_3",
                    level = CefrLevel.A1,
                    unitNumber = 3,
                    title = "Basic Grammar: Present Tense 'To Be'",
                    description = "Understand the fundamental verb conjugations (Ser vs Estar).",
                    skillType = SkillType.GRAMMAR,
                    xpReward = 30,
                    exercises = listOf(
                        Exercise(
                            id = "ex_a1_3_1",
                            type = ExerciseType.MULTIPLE_CHOICE,
                            question = "Yo _____ profesor y ahora _____ en la biblioteca.",
                            instruction = "Choose the correct pair of Ser and Estar",
                            options = listOf("soy / estoy", "estoy / soy", "es / está", "somos / estamos"),
                            correctAnswer = "soy / estoy",
                            audioPhrase = "Yo soy profesor y ahora estoy en la biblioteca",
                            translation = "I am a professor and now I am in the library",
                            explanation = "Use 'Ser' for identity/profession and 'Estar' for temporary location/state."
                        )
                    )
                )
            )

            CefrLevel.A2 -> listOf(
                Lesson(
                    id = "${languageId}_a2_1",
                    level = CefrLevel.A2,
                    unitNumber = 1,
                    title = "Past Experiences & Memories",
                    description = "Recount past events, holidays, and childhood memories using past tenses.",
                    skillType = SkillType.GRAMMAR,
                    xpReward = 35,
                    exercises = listOf(
                        Exercise(
                            id = "ex_a2_1_1",
                            type = ExerciseType.MULTIPLE_CHOICE,
                            question = "Ayer nosotros _____ a un restaurante fantástico.",
                            instruction = "Select the preterite past tense",
                            options = listOf("fuimos", "vamos", "íbamos", "ido"),
                            correctAnswer = "fuimos",
                            audioPhrase = "Ayer nosotros fuimos a un restaurante fantástico",
                            translation = "Yesterday we went to a fantastic restaurant"
                        ),
                        Exercise(
                            id = "ex_a2_1_2",
                            type = ExerciseType.SENTENCE_BUILDER,
                            question = "Build: 'Last week I visited my family'",
                            instruction = "Tap words in order",
                            options = listOf("La", "semana", "pasada", "visité", "a", "mi", "familia"),
                            correctAnswer = "La semana pasada visité a mi familia",
                            audioPhrase = "La semana pasada visité a mi familia",
                            translation = "Last week I visited my family"
                        )
                    )
                ),
                Lesson(
                    id = "${languageId}_a2_2",
                    level = CefrLevel.A2,
                    unitNumber = 2,
                    title = "Asking Directions & Transport",
                    description = "Navigate public transit, subway maps, and asking locals for directions.",
                    skillType = SkillType.CONVERSATION,
                    xpReward = 35,
                    exercises = listOf(
                        Exercise(
                            id = "ex_a2_2_1",
                            type = ExerciseType.MULTIPLE_CHOICE,
                            question = "¿Disculpe, para ir al museo central?",
                            instruction = "Select the most natural response",
                            options = listOf("Siga todo recto y gire a la derecha", "No tengo billete de tren", "El museo cuesta diez dólares", "Hace buen tiempo hoy"),
                            correctAnswer = "Siga todo recto y gire a la derecha",
                            audioPhrase = "Siga todo recto y gire a la derecha",
                            translation = "Go straight ahead and turn right"
                        )
                    )
                )
            )

            CefrLevel.B1 -> listOf(
                Lesson(
                    id = "${languageId}_b1_1",
                    level = CefrLevel.B1,
                    unitNumber = 1,
                    title = "Expressing Opinions & Debates",
                    description = "Defend viewpoints, express agreement or polite disagreement with nuance.",
                    skillType = SkillType.SPEAKING,
                    xpReward = 40,
                    exercises = listOf(
                        Exercise(
                            id = "ex_b1_1_1",
                            type = ExerciseType.MULTIPLE_CHOICE,
                            question = "Desde mi punto de vista, la tecnología _____ nuestras vidas.",
                            instruction = "Choose the best verb",
                            options = listOf("ha transformado radicalmente", "es muy blanco", "olvidará ayer", "no sabe nadar"),
                            correctAnswer = "ha transformado radicalmente",
                            audioPhrase = "Desde mi punto de vista, la tecnología ha transformado radicalmente nuestras vidas",
                            translation = "From my point of view, technology has radically transformed our lives"
                        )
                    )
                )
            )

            CefrLevel.B2 -> listOf(
                Lesson(
                    id = "${languageId}_b2_1",
                    level = CefrLevel.B2,
                    unitNumber = 1,
                    title = "Professional Correspondence & Negotiations",
                    description = "Write business emails, negotiate agreements, and explain market trends.",
                    skillType = SkillType.WRITING,
                    xpReward = 45,
                    exercises = listOf(
                        Exercise(
                            id = "ex_b2_1_1",
                            type = ExerciseType.MULTIPLE_CHOICE,
                            question = "Le agradecemos de antemano su pronta _____ a esta propuesta.",
                            instruction = "Choose formal business terminology",
                            options = listOf("respuesta", "canción", "comida", "calle"),
                            correctAnswer = "respuesta",
                            audioPhrase = "Le agradecemos de antemano su pronta respuesta a esta propuesta",
                            translation = "We thank you in advance for your prompt response to this proposal"
                        )
                    )
                )
            )

            CefrLevel.C1 -> listOf(
                Lesson(
                    id = "${languageId}_c1_1",
                    level = CefrLevel.C1,
                    unitNumber = 1,
                    title = "Idiomatic Fluency & Rhetoric",
                    description = "Master cultural idioms, sophisticated humor, and persuasive rhetoric.",
                    skillType = SkillType.READING,
                    xpReward = 50,
                    exercises = listOf(
                        Exercise(
                            id = "ex_c1_1_1",
                            type = ExerciseType.MULTIPLE_CHOICE,
                            question = "El orador abordó el asunto 'sin pelos en la lengua'. ¿Qué significa?",
                            instruction = "Identify the idiomatic meaning",
                            options = listOf("Habló con total franqueza y sin tapujos", "Tenía problemas de pronunciación", "Habló en voz muy baja", "No quería contestar preguntas"),
                            correctAnswer = "Habló con total franqueza y sin tapujos",
                            audioPhrase = "El orador habló sin pelos en la lengua sobre los retos económicos",
                            translation = "The speaker spoke with total candor about economic challenges"
                        )
                    )
                )
            )

            CefrLevel.C2 -> listOf(
                Lesson(
                    id = "${languageId}_c2_1",
                    level = CefrLevel.C2,
                    unitNumber = 1,
                    title = "Literary Stylistics & Nuance",
                    description = "Appreciate archaic expressions, literary prose, and subtle cultural subtexts.",
                    skillType = SkillType.SPEAKING,
                    xpReward = 60,
                    exercises = listOf(
                        Exercise(
                            id = "ex_c2_1_1",
                            type = ExerciseType.MULTIPLE_CHOICE,
                            question = "Identifique el término que denota la cualidad de lo efímero y transitorio:",
                            instruction = "Select the advanced literary synonym",
                            options = listOf("Fugacidad", "Perpetuidad", "Inmutabilidad", "Constancia"),
                            correctAnswer = "Fugacidad",
                            audioPhrase = "La fugacidad del tiempo es el leitmotiv de su obra lírica",
                            translation = "The transience of time is the leitmotif of his lyrical work"
                        )
                    )
                )
            )
        }
    }

    val conversationTopics = listOf(
        ConversationTopic(
            id = "conv_travel",
            title = "Airport & Flight Boarding",
            situation = "At the International Airport",
            iconEmoji = "✈️",
            level = CefrLevel.A1,
            description = "Check your bags, navigate terminal security, and find your gate.",
            dialogues = listOf(
                DialogueLine("Officer", false, "Good afternoon. Passport and boarding pass, please.", "Buenas tardes. Pasaporte y tarjeta de embarque, por favor."),
                DialogueLine("You", true, "Here you are. Is flight IB340 on schedule?", "Aquí tiene. ¿El vuelo IB340 está en horario?"),
                DialogueLine("Officer", false, "Yes, it is on time. Gate B24 begins boarding at 15:45.", "Sí, está a tiempo. La puerta B24 comienza el embarque a las 15:45."),
                DialogueLine("You", true, "Thank you very much. Where is security check?", "Muchas gracias. ¿Dónde está el control de seguridad?"),
                DialogueLine("Officer", false, "Walk straight ahead and turn to your left.", "Camine recto y gire a su izquierda.")
            )
        ),
        ConversationTopic(
            id = "conv_hotel",
            title = "Hotel Check-In & Service",
            situation = "At the Hotel Reception",
            iconEmoji = "🏨",
            level = CefrLevel.A2,
            description = "Check into your room, ask for Wi-Fi details, and request breakfast.",
            dialogues = listOf(
                DialogueLine("Receptionist", false, "Welcome to Grand Palace Hotel. Do you have a reservation?", "Bienvenido al Hotel Gran Palace. ¿Tiene una reserva?"),
                DialogueLine("You", true, "Yes, a double room under the name Ahmed for four nights.", "Sí, una habitación doble a nombre de Ahmed por cuatro noches."),
                DialogueLine("Receptionist", false, "Splendid! Room 402 with city view. Breakfast is served from 7:00 to 10:30 AM.", "¡Estupendo! Habitación 402 con vista a la ciudad. El desayuno se sirve de 7 a 10:30."),
                DialogueLine("You", true, "Could you please give me the Wi-Fi password?", "¿Podría darme la contraseña de la red wifi?"),
                DialogueLine("Receptionist", false, "Certainly. It is written on your key card envelope.", "Por supuesto. Está anotada en el sobre de su tarjeta llave.")
            )
        ),
        ConversationTopic(
            id = "conv_restaurant",
            title = "Gourmet Restaurant & Orders",
            situation = "Dining Out at Bistro",
            iconEmoji = "🍽️",
            level = CefrLevel.A2,
            description = "Reserve a table, inquire about daily specials, and order dishes.",
            dialogues = listOf(
                DialogueLine("Waiter", false, "Good evening! Welcome. Table for how many?", "¡Buenas noches! Bienvenidos. ¿Mesa para cuántos?"),
                DialogueLine("You", true, "A table for two near the terrace, please.", "Una mesa para dos cerca de la terraza, por favor."),
                DialogueLine("Waiter", false, "Right this way. Today's chef special is grilled sea bass with saffron rice.", "Por aquí. El plato especial de hoy es lubina a la parrilla con arroz con azafrán."),
                DialogueLine("You", true, "Sounds delicious! We will take that, and a sparkling water.", "¡Suena delicioso! Tomaremos eso y un agua con gas.")
            )
        ),
        ConversationTopic(
            id = "conv_shopping",
            title = "Boutique Shopping & Sizes",
            situation = "In a Fashion Boutique",
            iconEmoji = "🛍️",
            level = CefrLevel.A2,
            description = "Find sizes, ask for fitting rooms, and inquire about discounts.",
            dialogues = listOf(
                DialogueLine("Assistant", false, "Hello! Can I help you find something today?", "¡Hola! ¿Puedo ayudarle a encontrar algo hoy?"),
                DialogueLine("You", true, "Yes, I really like this jacket. Do you have it in medium?", "Sí, me gusta mucho esta chaqueta. ¿La tienen en talla mediana?"),
                DialogueLine("Assistant", false, "Let me check our stock... Yes, here is medium! Fitting rooms are to the back.", "Deje revisar el stock... ¡Sí, aquí está la mediana! Los probadores están al fondo."),
                DialogueLine("You", true, "It fits great. Do you accept credit card payment?", "Me queda perfecta. ¿Aceptan pago con tarjeta de crédito?")
            )
        ),
        ConversationTopic(
            id = "conv_interview",
            title = "Job Interview & Career",
            situation = "Professional Career Interview",
            iconEmoji = "💼",
            level = CefrLevel.B2,
            description = "Present your achievements, strengths, and discuss role responsibilities.",
            dialogues = listOf(
                DialogueLine("Interviewer", false, "Welcome. Could you walk us through your background in software engineering?", "Bienvenido. ¿Podría detallarnos su trayectoria en ingeniería de software?"),
                DialogueLine("You", true, "Over the past 5 years, I led cross-functional teams developing mobile applications with high scalability.", "Durante los últimos cinco años, lideré equipos multidisciplinarios creando apps móviles escalables."),
                DialogueLine("Interviewer", false, "Impressive. How do you handle tight project deadlines under pressure?", "Impresionante. ¿Cómo gestiona plazos ajustados bajo presión?"),
                DialogueLine("You", true, "I prioritize critical user flows, foster transparent team communication, and automate testing.", "Priorizo flujos clave, fomento comunicación transparente y automatizo pruebas.")
            )
        ),
        ConversationTopic(
            id = "conv_education",
            title = "University & Campus Life",
            situation = "University Library & Lecture Hall",
            iconEmoji = "🎓",
            level = CefrLevel.B1,
            description = "Inquire about research books, seminar credits, and academic topics.",
            dialogues = listOf(
                DialogueLine("Librarian", false, "Can I help you locate research materials?", "¿Puedo ayudarte a localizar material de investigación?"),
                DialogueLine("You", true, "Yes, I need the latest publications on cognitive linguistics.", "Sí, necesito las últimas publicaciones sobre lingüística cognitiva."),
                DialogueLine("Librarian", false, "They are archived in the digital section on the third floor.", "Están archivadas en la sección digital del tercer piso.")
            )
        ),
        ConversationTopic(
            id = "conv_networking",
            title = "Meeting People & Socializing",
            situation = "International Networking Event",
            iconEmoji = "👋",
            level = CefrLevel.A2,
            description = "Break the ice, discuss passions, and exchange contacts.",
            dialogues = listOf(
                DialogueLine("Sophie", false, "Hi! Is this seat taken? Wonderful keynote, wasn't it?", "¡Hola! ¿Está ocupado este asiento? Gran conferencia, ¿verdad?"),
                DialogueLine("You", true, "Not at all, please join! Yes, the speaker brought fascinating insights.", "Para nada, ¡siéntate! Sí, el ponente aportó ideas fascinantes."),
                DialogueLine("Sophie", false, "What field are you working in?", "¿En qué campo estás trabajando?"),
                DialogueLine("You", true, "I am passionate about language technology and cross-cultural communication.", "Me apasiona la tecnología del lenguaje y la comunicación intercultural.")
            )
        ),
        ConversationTopic(
            id = "conv_daily",
            title = "Daily Life & Health Consultation",
            situation = "Pharmacy & Doctor's Clinic",
            iconEmoji = "🏠",
            level = CefrLevel.B1,
            description = "Describe symptoms, ask for medication, and understand dosages.",
            dialogues = listOf(
                DialogueLine("Pharmacist", false, "Good morning. How can I help you today?", "Buenos días. ¿En qué puedo ayudarle hoy?"),
                DialogueLine("You", true, "I have had a sore throat and fever since yesterday evening.", "Tengo dolor de garganta y fiebre desde ayer por la tarde."),
                DialogueLine("Pharmacist", false, "Take these soothing lozenges every 8 hours after meals.", "Tome estas pastillas cada ocho horas después de las comidas.")
            )
        )
    )

    val sampleVocabulary = listOf(
        VocabularyWord("v1", "Desafío", "Challenge", "/de.saˈfi.o/", "Noun", "Aprender un nuevo idioma es un hermoso desafío.", "Learning a new language is a beautiful challenge.", CefrLevel.B1),
        VocabularyWord("v2", "Sostenibilidad", "Sustainability", "/sos.te.ni.bi.liˈdad/", "Noun", "La sostenibilidad energética es clave para el futuro.", "Energy sustainability is key for the future.", CefrLevel.B2),
        VocabularyWord("v3", "Imprescindible", "Essential / Indispensable", "/im.pɾe.sinˈdi.ble/", "Adjective", "El agua es imprescindible para todos los seres vivos.", "Water is essential for all living beings.", CefrLevel.B2),
        VocabularyWord("v4", "Agradecido", "Grateful", "/a.ɣɾa.ðeˈsi.ðo/", "Adjective", "Estoy muy agradecido por su valiosa ayuda.", "I am very grateful for your valuable help.", CefrLevel.A2),
        VocabularyWord("v5", "Entorno", "Environment / Surroundings", "/enˈtoɾ.no/", "Noun", "Un entorno positivo estimula la creatividad.", "A positive environment stimulates creativity.", CefrLevel.B1),
        VocabularyWord("v6", "Elocuente", "Eloquent", "/e.loˈkwen.te/", "Adjective", "Pronunció un discurso sumamente elocuente y conmovedor.", "He delivered a supremely eloquent and moving speech.", CefrLevel.C1),
        VocabularyWord("v7", "Cotidianidad", "Everyday life", "/ko.ti.ðja.niˈðað/", "Noun", "Encontramos poesía en los detalles de la cotidianidad.", "We find poetry in the details of everyday life.", CefrLevel.C1),
        VocabularyWord("v8", "Bienvenida", "Welcome", "/bjen.beˈni.ða/", "Noun", "Les damos una cordial bienvenida a nuestro evento.", "We give you a warm welcome to our event.", CefrLevel.A1)
    )

    val placementQuestions = listOf(
        PlacementQuestion("pq1", "Completa la frase básica: 'Hola, ¿cómo _____?'", listOf("te llamas", "comer", "donde", "hoy"), 0, CefrLevel.A1, "Greetings"),
        PlacementQuestion("pq2", "¿Cuál es el plural correcto de 'el lápiz'?", listOf("los lápizes", "los lápices", "las lápices", "los lápiz"), 1, CefrLevel.A1, "Grammar"),
        PlacementQuestion("pq3", "Ayer no _____ ir a clase porque me sentía enfermo.", listOf("pude", "puedo", "podré", "pudiendo"), 0, CefrLevel.A2, "Past Tense"),
        PlacementQuestion("pq4", "Si mañana hace buen tiempo, nosotros _____ a la playa.", listOf("iremos", "fuimos", "iríamos", "vamos ido"), 0, CefrLevel.A2, "Future / Conditional"),
        PlacementQuestion("pq5", "Dudo que ellos _____ a tiempo para la reunión.", listOf("lleguen", "llegan", "llegaron", "llegarán"), 0, CefrLevel.B1, "Subjunctive"),
        PlacementQuestion("pq6", "El proyecto fue cancelado _____ falta de presupuesto.", listOf("por", "para", "según", "hacia"), 0, CefrLevel.B1, "Prepositions"),
        PlacementQuestion("pq7", "A pesar de las dificultades, el equipo logró _____ sus metas.", listOf("alcanzar", "rendirse", "ignorar", "demoler"), 0, CefrLevel.B2, "Advanced Vocabulary"),
        PlacementQuestion("pq8", "Ojalá que todos los delegados _____ con las conclusiones.", listOf("coincidieran", "coinciden", "coincidirán", "coincidiendo"), 0, CefrLevel.B2, "Imperfect Subjunctive"),
        PlacementQuestion("pq9", "Es una persona sumamente escrupulosa; siempre cuida hasta el más mínimo _____.", listOf("pormenor", "escándalo", "ruido", "abismo"), 0, CefrLevel.C1, "Nuanced Expressions"),
        PlacementQuestion("pq10", "Dicho argumento carece de fundamento y resulta a todas luces _____.", listOf("inverosímil", "cotidiano", "festivo", "amable"), 0, CefrLevel.C1, "Rhetoric"),
        PlacementQuestion("pq11", "En este tratado se desentrañan las _____ más intrincadas de la geopolítica.", listOf("vicisitudes", "comidas", "ventanas", "calles"), 0, CefrLevel.C2, "Literary Precision"),
        PlacementQuestion("pq12", "Aquella elucubración filosófica puso de manifiesto su insondable _____.", listOf("erudición", "descuido", "rapidez", "cansancio"), 0, CefrLevel.C2, "Mastery")
    )

    fun getExamQuestions(level: CefrLevel): List<ExamQuestion> {
        return when (level) {
            CefrLevel.A1 -> listOf(
                ExamQuestion("eq_a1_1", "Vocabulary", "¿Cuál es el antónimo de 'grande'?", listOf("Pequeño", "Alto", "Rápido", "Lejos"), 0),
                ExamQuestion("eq_a1_2", "Grammar", "Ellos _____ de Madrid, España.", listOf("son", "están", "es", "somos"), 0),
                ExamQuestion("eq_a1_3", "Listening", "Escuche el saludo formal:", listOf("Buenos días, señor García", "Hola amigo", "¿Qué tal todo?", "Hasta luego"), 0, "Buenos días, señor García"),
                ExamQuestion("eq_a1_4", "Reading", "Significado de 'Por favor':", listOf("Please", "Thank you", "Goodbye", "Excuse me"), 0),
                ExamQuestion("eq_a1_5", "Speaking & Pronunciation", "Frase cortés al despedirse:", listOf("Hasta luego, que tenga buen día", "Ven aquí", "No quiero", "Dámelo"), 0)
            )
            CefrLevel.A2 -> listOf(
                ExamQuestion("eq_a2_1", "Grammar", "Cuando era niño, siempre _____ en el parque.", listOf("jugaba", "jugué", "juego", "jugaré"), 0),
                ExamQuestion("eq_a2_2", "Vocabulary", "¿Dónde compras medicinas?", listOf("En la farmacia", "En la biblioteca", "En el cine", "En el gimnasio"), 0),
                ExamQuestion("eq_a2_3", "Reading", "Texto: 'El vuelo se ha retrasado 30 minutos'. ¿Qué ocurre?", listOf("Llegará más tarde", "Ha sido cancelado", "Sale antes", "Cambió de destino"), 0),
                ExamQuestion("eq_a2_4", "Listening", "¿Qué pide el cliente?", listOf("Un café con leche caliente", "Una ensalada verde", "Un refresco frío", "La cuenta"), 0, "Quisiera un café con leche caliente, por favor"),
                ExamQuestion("eq_a2_5", "Writing", "Escribe la forma correcta: 'Nosotros ya hemos _____ (terminar).'", listOf("terminado", "terminando", "terminar", "terminaron"), 0)
            )
            CefrLevel.B1 -> listOf(
                ExamQuestion("eq_b1_1", "Grammar", "No creo que él _____ la verdad.", listOf("diga", "dice", "dirá", "dijo"), 0),
                ExamQuestion("eq_b1_2", "Vocabulary", "Una persona que no se rinde ante los obstáculos es:", listOf("Perseverante", "Impuntual", "Indecisa", "Perezosa"), 0),
                ExamQuestion("eq_b1_3", "Reading", "¿Cuál es la idea principal sobre energías renovables?", listOf("Reducir emisiones contaminantes", "Aumentar costes de carbón", "Eliminar el transporte", "Cerrar fábricas"), 0),
                ExamQuestion("eq_b1_4", "Listening", "Identifique el tono de la queja:", listOf("Reclamación cortés por servicio", "Agradecimiento cálido", "Invitación a fiesta", "Oferta comercial"), 0, "Estimado gerente, escribo para manifestar mi inconformidad con el retraso"),
                ExamQuestion("eq_b1_5", "Conversations", "Para expresar acuerdo moderado:", listOf("Estoy de acuerdo en gran medida", "Es completamente falso", "No me importa nada", "Qué tontería"), 0)
            )
            CefrLevel.B2 -> listOf(
                ExamQuestion("eq_b2_1", "Grammar", "De haberlo sabido antes, te _____ inmediatamente.", listOf("habría avisado", "había avisado", "aviso", "avisando"), 0),
                ExamQuestion("eq_b2_2", "Vocabulary", "Sinónimo culto de 'controversia':", listOf("Polémica", "Armonía", "Consenso", "Certeza"), 0),
                ExamQuestion("eq_b2_3", "Reading", "¿Qué subraya el análisis financiero?", listOf("La volatilidad del mercado bursátil", "La abundancia de crédito", "El estancamiento agrícola", "La bajada de impuestos"), 0),
                ExamQuestion("eq_b2_4", "Listening", "¿Cuál es el consejo del consultor?", listOf("Diversificar las fuentes de inversión", "Gastar todo el capital", "Despedir al personal", "Detener la innovación"), 0, "Es primordial diversificar el portafolio para mitigar riesgos sistémicos"),
                ExamQuestion("eq_b2_5", "Writing", "Fórmula de cortesía formal en correspondencia:", listOf("Sin otro particular, le saluda atentamente", "Nos vemos luego colega", "Chau chau", "Mándame eso ya"), 0)
            )
            CefrLevel.C1 -> listOf(
                ExamQuestion("eq_c1_1", "Grammar", "Por mucho que _____ esforzado, las circunstancias fueron adversas.", listOf("se hubiera", "se ha", "se tenía", "se estaba"), 0),
                ExamQuestion("eq_c1_2", "Vocabulary", "Término para 'decisión irrevocable que cambia el destino':", listOf("Rubicón / Hito crucial", "Paseo banal", "Rutina diaria", "Detalle trivial"), 0),
                ExamQuestion("eq_c1_3", "Reading", "¿Qué matiz crítico aporta el autor sobre la modernidad líquida?", listOf("La fragilidad de los vínculos humanos", "La solidez de las instituciones", "El declive de la tecnología", "La victoria del comunitarismo"), 0),
                ExamQuestion("eq_c1_4", "Listening", "Reconozca la sutileza irónica en la alocución:", listOf("Ironía sutil hacia los pronósticos optimistas", "Elogio sincero y desmedido", "Indiferencia total", "Desconocimiento del tema"), 0, "Es verdaderamente fascinante cómo ciertos expertos nunca se equivocan... excepto cuando predicen el futuro"),
                ExamQuestion("eq_c1_5", "Stylistics", "Recurso retórico en 'un silencio ensordecedor':", listOf("Oxímoron", "Pleonasmo", "Hipérbole", "Elipsis"), 0)
            )
            CefrLevel.C2 -> listOf(
                ExamQuestion("eq_c2_1", "Grammar & Syntax", "¿Cuál construcción ilustra el hipérbaton gongorino?", listOf("De este, pues, formidable de la tierra bostezo", "El sol brilla en el cielo azul", "Ayer comí manzanas dulces", "Vamos a la fiesta con Juan"), 0),
                ExamQuestion("eq_c2_2", "Lexicon", "Antónimo exacto de 'perínclito':", listOf("Desconocido / Ignoto / Infame", "Ilustre", "Célebre", "Insigne"), 0),
                ExamQuestion("eq_c2_3", "Hermeneutics", "Interpretación de 'la aporía discursiva':", listOf("Una encrucijada lógica o contradicción insoluble", "Una demostración geométrica simple", "Un error gramatical menor", "Un refrán popular"), 0),
                ExamQuestion("eq_c2_4", "Listening", "Análisis del registro sociolingüístico:", listOf("Discurso erudito de alta formalidad académica", "Jerga juvenil callejera", "Lenguaje publicitario básico", "Conversación de compras"), 0, "Huelga elucidar los intrincados corolarios que dimanan de semejante axioma hermenéutico"),
                ExamQuestion("eq_c2_5", "Nuance", "Distinción entre 'inerme' e 'inerte':", listOf("Desarmado / indefenso vs inmóvil / sin vida", "Grande vs pequeño", "Rápido vs lento", "Alegre vs triste"), 0)
            )
        }
    }
}

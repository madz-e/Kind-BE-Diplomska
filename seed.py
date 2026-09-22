#!/usr/bin/env python3
from __future__ import annotations
"""
Kind Mental Health App - Database Seeder
Seeds 10 users with ~4 months of historical data suitable for LLM analysis.

Usage:
    pip install requests
    python seed.py [--base-url http://localhost:8080]

Prerequisites:
    - Spring Boot backend running
    - PostgreSQL database accessible with schema created (DDL auto=update handles this)
"""

import requests
import random
import math
import sys
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import date, timedelta

random.seed(42)

BASE_URL = "http://localhost:8080"
if "--base-url" in sys.argv:
    idx = sys.argv.index("--base-url")
    BASE_URL = sys.argv[idx + 1]

# ==================== REFERENCE DATA ====================

EMOTIONS_DATA = [
    {"name": "Devastated",    "moodCategory": "VERY_UNPLEASANT"},
    {"name": "Hopeless",      "moodCategory": "VERY_UNPLEASANT"},
    {"name": "Terrified",     "moodCategory": "VERY_UNPLEASANT"},
    {"name": "Miserable",     "moodCategory": "VERY_UNPLEASANT"},
    {"name": "Grief-stricken","moodCategory": "VERY_UNPLEASANT"},  # NEW
    {"name": "Panicked",      "moodCategory": "VERY_UNPLEASANT"},  # NEW
    {"name": "Numb",          "moodCategory": "VERY_UNPLEASANT"},  # NEW
    {"name": "Heartbroken",   "moodCategory": "VERY_UNPLEASANT"},  # NEW
    {"name": "Sad",           "moodCategory": "UNPLEASANT"},
    {"name": "Anxious",       "moodCategory": "UNPLEASANT"},
    {"name": "Frustrated",    "moodCategory": "UNPLEASANT"},
    {"name": "Overwhelmed",   "moodCategory": "UNPLEASANT"},
    {"name": "Lonely",        "moodCategory": "UNPLEASANT"},
    {"name": "Worried",       "moodCategory": "UNPLEASANT"},
    {"name": "Irritated",     "moodCategory": "UNPLEASANT"},  # NEW
    {"name": "Disappointed",  "moodCategory": "UNPLEASANT"},  # NEW
    {"name": "Insecure",      "moodCategory": "UNPLEASANT"},  # NEW
    {"name": "Restless",      "moodCategory": "UNPLEASANT"},  # NEW
    {"name": "Okay",          "moodCategory": "NEUTRAL"},
    {"name": "Calm",          "moodCategory": "NEUTRAL"},
    {"name": "Indifferent",   "moodCategory": "NEUTRAL"},
    {"name": "Tired",         "moodCategory": "NEUTRAL"},
    {"name": "Contemplative", "moodCategory": "NEUTRAL"},
    {"name": "Curious",       "moodCategory": "NEUTRAL"},  # NEW
    {"name": "Distracted",    "moodCategory": "NEUTRAL"},  # NEW
    {"name": "Bored",         "moodCategory": "NEUTRAL"},  # NEW
    {"name": "Reflective",    "moodCategory": "NEUTRAL"},  # NEW
    {"name": "Happy",         "moodCategory": "PLEASANT"},
    {"name": "Energetic",     "moodCategory": "PLEASANT"},
    {"name": "Grateful",      "moodCategory": "PLEASANT"},
    {"name": "Hopeful",       "moodCategory": "PLEASANT"},
    {"name": "Content",       "moodCategory": "PLEASANT"},
    {"name": "Relaxed",       "moodCategory": "PLEASANT"},
    {"name": "Proud",         "moodCategory": "PLEASANT"},  # NEW
    {"name": "Motivated",     "moodCategory": "PLEASANT"},  # NEW
    {"name": "Optimistic",    "moodCategory": "PLEASANT"},  # NEW
    {"name": "Playful",       "moodCategory": "PLEASANT"},  # NEW
    {"name": "Joyful",        "moodCategory": "VERY_PLEASANT"},
    {"name": "Excited",       "moodCategory": "VERY_PLEASANT"},
    {"name": "Inspired",      "moodCategory": "VERY_PLEASANT"},
    {"name": "Ecstatic",      "moodCategory": "VERY_PLEASANT"},
    {"name": "Blissful",      "moodCategory": "VERY_PLEASANT"},
    {"name": "Euphoric",      "moodCategory": "VERY_PLEASANT"},  # NEW
    {"name": "Triumphant",    "moodCategory": "VERY_PLEASANT"},  # NEW
    {"name": "Radiant",       "moodCategory": "VERY_PLEASANT"},  # NEW
    {"name": "Overjoyed",     "moodCategory": "VERY_PLEASANT"},  # NEW
]

MOOD_FACTORS_DATA = [
    {"name": "Work stress"},
    {"name": "Sleep deprivation"},
    {"name": "Exercise"},
    {"name": "Social interaction"},
    {"name": "Diet"},
    {"name": "Weather"},
    {"name": "Family issues"},
    {"name": "Financial concerns"},
    {"name": "Health issues"},
    {"name": "Achievements"},
    {"name": "Relaxation"},
    {"name": "Nature"},
    {"name": "Creativity"},
    {"name": "Learning"},
    {"name": "Relationship issues"},
    {"name": "Self-care"},
]

JOURNAL_PROMPTS_DATA = [
    {"promptText": "What are three things you're grateful for today?",                             "type": "GENERAL"},
    {"promptText": "What did you accomplish today, no matter how small?",                          "type": "GENERAL"},
    {"promptText": "What made you smile today?",                                                   "type": "GENERAL"},
    {"promptText": "What challenge are you currently facing and how might you overcome it?",       "type": "GENERAL"},
    {"promptText": "How did you take care of yourself today?",                                     "type": "GENERAL"},
    {"promptText": "What are you looking forward to this week?",                                   "type": "GENERAL"},
    {"promptText": "Describe a moment today when you felt at peace.",                              "type": "GENERAL"},
    {"promptText": "What would you tell your past self from a year ago?",                          "type": "GENERAL"},
    {"promptText": "Let's work through an automatic negative thought that's been bothering you.",  "type": "ANT_EXERCISE"},
    {"promptText": "Identify a recurring negative thought pattern and challenge it.",              "type": "ANT_EXERCISE"},
]

# ==================== JOURNAL CONTENT TEMPLATES ====================

JOURNAL_BLANK = {
    "low": [
        (
            "Today was really tough",
            "I couldn't shake the feeling that everything is going wrong. I didn't get enough sleep last night and woke up already feeling drained. Work was overwhelming and I felt like I couldn't keep up. I'm trying to remind myself that tomorrow is a new day, but right now it's hard to see through the fog.",
        ),
        (
            "Struggling today",
            "I've been struggling a lot lately. It feels like I'm carrying a weight on my chest that just won't lift. Simple tasks feel monumental. I canceled plans with friends again because I just didn't have the energy. I know I need to reach out for help, but it's so hard when everything feels hopeless.",
        ),
        (
            "Hard day",
            "Another difficult day. The anxiety has been particularly bad this week. My mind keeps racing with worst-case scenarios. I tried the breathing exercises but couldn't focus. I'm going to try to get to bed early and hope tomorrow is better. Journaling helps a little at least.",
        ),
        (
            "Feeling disconnected",
            "I feel so alone right now. Even surrounded by people I still feel disconnected. Work has been a source of so much stress and I just don't know how much longer I can keep pushing through. I'm wondering if I should talk to someone about how I've been feeling.",
        ),
        (
            "Late night thoughts",
            "Can't sleep again. It's late and my mind won't stop. Worried about so many things — work, money, the future. I don't know how to make it stop. Just trying to breathe through it and get to morning.",
        ),
    ],
    "medium_low": [
        (
            "Small wins",
            "Today had its ups and downs. The morning started rough but I managed to get through my tasks at work. Had a moment of anxiety mid-afternoon but used the 4-7-8 breathing technique and it helped. Small wins count.",
        ),
        (
            "Getting through it",
            "Feeling a bit better than yesterday. Still not great, but I managed to go for a short walk outside which helped clear my head. Getting outside even for 15 minutes makes a real difference.",
        ),
        (
            "Going through the motions",
            "It's been one of those days where I'm just going through the motions. Not bad, not good. I completed my morning routine which felt like an accomplishment given how unmotivated I've been. I'm noticing that my tracked habits give the day some structure.",
        ),
        (
            "Looking for joy",
            "I've been trying to focus on the small moments of joy. Found myself actually laughing at something today. That felt good. Still carrying a lot of worry about the future but I'm practicing sitting with uncertainty rather than fighting it.",
        ),
        (
            "Slow progress",
            "Made it through the day. Used my coping skills when I started to spiral — did some deep breathing, went for a short walk. It's slow progress but it is progress. Reminded myself: I don't have to feel great, I just have to keep going.",
        ),
    ],
    "medium": [
        (
            "Pretty okay day",
            "Pretty neutral day overall. Got through work without too much stress, had lunch with a friend which was nice. I've been trying to practice mindfulness and I'm starting to see small improvements in how I handle stressful situations.",
        ),
        (
            "Steady",
            "Today felt steady and manageable. I completed my morning routine for the 10th day in a row which felt good. Starting to notice that when I skip it, my days feel more chaotic. Something small but it matters.",
        ),
        (
            "Rest is productive too",
            "A calm and uneventful day. Sometimes that's exactly what you need. I spent the evening reading and doing nothing 'productive,' and for once I didn't feel guilty about it. Learning that rest is productive too.",
        ),
        (
            "Noticing patterns",
            "Today I felt balanced. There were moments of stress but also moments of genuine contentment. I'm noticing patterns in what affects my mood — sleep and exercise seem to have the biggest impact. Going to keep tracking these.",
        ),
        (
            "Finding my rhythm",
            "Nothing major happened today but I feel like I'm finding a rhythm. Morning routine done, decent sleep last night, ate well. The basics matter more than I used to give them credit for.",
        ),
    ],
    "medium_high": [
        (
            "Good day",
            "Good day today! Felt productive at work and had the energy to also cook a nice dinner. Had a great conversation with a friend that really boosted my mood. Feeling cautiously optimistic about some things at work.",
        ),
        (
            "Finally rested",
            "Woke up feeling actually rested for once — that makes such a huge difference. Had a really productive morning and even went to the gym after work. Feeling grateful for days like this. I'm starting to see the progress I've been working toward.",
        ),
        (
            "Building momentum",
            "Today was genuinely good. Completed all my tasks, had a lovely lunch outside in the sunshine, and my meditation practice felt really deep. I'm building real momentum with my habits and it's starting to show in my energy levels.",
        ),
        (
            "Coming back to myself",
            "Really solid day. I feel like I'm finding my footing again. The past few months have been challenging but I can see how far I've come. Had a meaningful conversation about mental health which felt vulnerable but important.",
        ),
        (
            "Grateful",
            "Feeling grateful today. Had a moment where I realized how much has shifted — I'm handling stress better, sleeping more consistently, and actually enjoying things again. It didn't happen overnight but it's happening.",
        ),
    ],
    "high": [
        (
            "What a day!",
            "What a wonderful day! Everything just seemed to flow naturally. I was energetic, focused, and felt genuinely connected to the people around me. Days like this remind me why all the work I've been putting into my mental health is worth it.",
        ),
        (
            "Feeling fantastic",
            "I'm feeling fantastic! Had an amazing workout this morning, got great news at work, and spent the evening with people who energize me. I'm so grateful for the support system I have and the progress I've been making.",
        ),
        (
            "On top of the world",
            "Feeling on top of the world today. I finished a project I've been working on for months and the feedback was incredible. I celebrated with my favorite restaurant. Life feels good right now.",
        ),
        (
            "Pure joy",
            "Pure joy today. I went hiking with friends and felt completely present and connected to the moment. No anxiety, no rumination — just being fully alive in the experience. This is the version of myself I want to be more often.",
        ),
        (
            "Everything clicking",
            "One of those rare days when everything clicks. Slept well, ate well, exercised, had meaningful conversations, did work I'm proud of. I want to hold onto this feeling and remember it on the harder days.",
        ),
    ],
}

ANT_JOURNAL_CONTENT = [
    """What happened? I made a mistake in a presentation at work and my boss noticed in front of everyone.

What emotions did I feel at the time? How intense were they?
Shame (9/10), embarrassment (8/10), anxiety (7/10).

What went through my mind?
"I'm incompetent. Everyone thinks I'm a fraud. I'm going to lose my job."

Facts that support this thought?
I did make an error. My boss did point it out in front of others.

Facts that provide evidence against this thought?
I've received positive feedback on my work many times before. One mistake doesn't define my entire performance. My boss has supported me in the past. Everyone makes mistakes sometimes.

What can be an alternative thought?
"I made a mistake today, which is human. I can learn from it, apologize if needed, and do better next time. My value as an employee isn't determined by one error."

How do I feel now?
Still a bit embarrassed (4/10) but much less anxious (3/10). I feel more able to move forward.""",

    """What happened? A friend didn't respond to my messages for three days.

What emotions did I feel at the time? How intense were they?
Hurt (7/10), worried (8/10), rejected (6/10).

What went through my mind?
"They're avoiding me. I must have done something wrong. Maybe they don't want to be friends anymore."

Facts that support this thought?
They did take a long time to respond. This is unusual for them.

Facts that provide evidence against this thought?
People get busy. They've been reliable in the past. I have no actual evidence I did something wrong. When they did respond, they explained they had been overwhelmed with family issues.

What can be an alternative thought?
"My friend was dealing with something difficult and it had nothing to do with me. I can check in with care rather than assuming the worst."

How do I feel now?
Much better. I'm going to reach out and ask how they're doing instead of spiraling.""",

    """What happened? I couldn't finish everything on my to-do list today.

What emotions did I feel at the time? How intense were they?
Failure (8/10), guilt (7/10), anxiety about tomorrow (6/10).

What went through my mind?
"I never get anything done. I'm so unproductive. I'm falling behind and it's only going to get worse."

Facts that support this thought?
There are unfinished tasks remaining. I started the day with a full list and didn't complete it all.

Facts that provide evidence against this thought?
I completed 7 out of 10 tasks — that's 70% completion. I had an unexpected two-hour meeting. I helped a colleague which was valuable. My to-do lists are often unrealistically long.

What can be an alternative thought?
"I accomplished a significant amount today while also adapting to unexpected demands. I can prioritize what carries forward to tomorrow. I am productive enough."

How do I feel now?
Still tired but no longer catastrophizing (2/10). Going to plan tomorrow's top 3 priorities and leave it at that.""",

    """What happened? I saw photos of friends having fun at a party I wasn't invited to.

What emotions did I feel at the time? How intense were they?
Hurt (8/10), left out (9/10), insecurity (7/10).

What went through my mind?
"Nobody really likes me. They have a whole life without me. I'm not important to them."

Facts that support this thought?
I wasn't included in this event. It looks like they had fun without me.

Facts that provide evidence against this thought?
I regularly spend time with these friends in other contexts. I wasn't invited because it was a separate work colleague's birthday party. People can have different social circles. I wasn't excluded maliciously.

What can be an alternative thought?
"My friends have full lives and different social circles, just as I do. Not being at every event doesn't mean I'm not valued. Our friendship is shown in many other ways."

How do I feel now?
Hurt reduced to about 3/10. I'm going to reach out to one of them to make plans, coming from a place of connection rather than insecurity.""",

    """What happened? I got critical feedback on a piece of work I was proud of.

What emotions did I feel at the time? How intense were they?
Disappointment (8/10), defensive (7/10), self-doubt (9/10).

What went through my mind?
"All my effort was worthless. I clearly don't know what I'm doing. I'll never be good enough."

Facts that support this thought?
The feedback pointed out real weaknesses in my work.

Facts that provide evidence against this thought?
Feedback is meant to help me grow, not to define my worth. I have received praise on past work. Even experts get critiques. The person giving feedback acknowledged the strengths too, which I almost ignored.

What can be an alternative thought?
"Getting detailed feedback means someone took my work seriously enough to engage with it deeply. I can use this to improve. Growth requires being willing to hear hard truths."

How do I feel now?
Disappointment down to 4/10. I'm going to read the feedback again tomorrow with fresh eyes and make a plan for revision.""",
]

PROMPT_JOURNAL_CONTENT = {
    "What are three things you're grateful for today?": [
        "1. My morning coffee and the quiet before the day starts.\n2. My friend texting to check in on me.\n3. That I finally finished something I'd been putting off for weeks.",
        "1. Sunlight coming through my window this morning.\n2. A kind message from my mom.\n3. The fact that I got through a hard week.",
        "1. My health, even when I don't feel great.\n2. The flexibility in my schedule today.\n3. A podcast that made my commute actually enjoyable.",
        "1. A coworker who covered for me when I needed it.\n2. A delicious meal I made from scratch.\n3. The breathing exercises that are genuinely helping me manage stress.",
        "1. That I have people in my life who care about me.\n2. A book I'm really enjoying right now.\n3. Having a safe place to sleep every night.",
    ],
    "What did you accomplish today, no matter how small?": [
        "I answered emails I'd been avoiding for days. I went for a 20-minute walk even though I didn't feel like it. I drank enough water. Small things, but they all count.",
        "I showed up to work on time even though I barely slept. I had one genuine, connecting conversation with someone. I didn't cancel my evening plans even though I was tempted.",
        "I completed my morning routine. I asked for help with something instead of struggling alone for hours. I ate three proper meals.",
        "I finished a report that's been hanging over me. I called my mom back. I went to bed at a reasonable time for the first time this week.",
        "I got out of bed when the alarm went off (which took real effort today). I did 10 minutes of meditation. I cooked instead of ordering takeout.",
    ],
    "What made you smile today?": [
        "A dog I passed on my walk this morning. My coworker's terrible pun that was so bad it made me laugh. A really good cup of tea in the afternoon.",
        "An unexpected message from an old friend. The way the light looked through the window at 5pm. Finishing something I'd been dreading and realizing it wasn't as bad as I thought.",
        "A child laughing loudly at a coffee shop. A small victory at work that nobody else noticed but felt huge to me. My favorite song coming on randomly.",
        "A good conversation that went deeper than I expected. My pet doing something ridiculous. Getting a compliment on something I worked hard on.",
        "The smell of rain this morning. A funny video a friend sent me. That quiet moment before everyone else wakes up when the day is still full of possibility.",
    ],
    "What challenge are you currently facing and how might you overcome it?": [
        "The main challenge is the constant feeling of being behind — at work, in my relationships, even with my health goals. I think I need to stop comparing my journey to others and focus on progress over perfection. One thing I can do today is pick just one priority instead of trying to tackle everything.",
        "I'm struggling with maintaining energy throughout the day. I think poor sleep is a big factor. I want to start a more consistent sleep schedule this week — same bedtime, phone away an hour before bed.",
        "Anxiety has been creeping back in. The challenge is catching it early before it spirals. I'm going to practice labeling the feeling when it appears and then doing a short breathing exercise before I react.",
        "I've been isolating myself lately which makes everything harder. The challenge is taking the first step toward connection when everything in me wants to withdraw. Maybe I'll text one person today — just a simple check-in.",
        "I'm dealing with uncertainty about the future and I keep trying to plan and control things I can't control. I want to practice staying present and taking action only on what's in my hands.",
    ],
    "How did you take care of yourself today?": [
        "I took a proper lunch break instead of eating at my desk. I did a 10-minute meditation in the afternoon when I felt myself getting tense. I turned my phone off for an hour in the evening.",
        "I went for a walk even though I didn't feel like it. I let myself say no to one thing that felt like too much. I got to bed on time.",
        "I cooked a proper meal instead of just snacking. I reached out to a friend instead of sitting alone with my thoughts. I did some stretching before bed.",
        "I gave myself permission to rest without feeling guilty about it. I spent time in nature. I journaled and it helped me process what's been going on.",
        "I noticed when I was starting to spiral and paused to breathe. I moved my body. I ate something nourishing. Small acts of care — but they add up.",
    ],
    "What are you looking forward to this week?": [
        "I'm looking forward to the weekend when I can sleep in and have nowhere to be. Also a dinner with a close friend on Thursday that I've been looking forward to for a while.",
        "There's a project at work I'm actually excited about for once. Also I'm going to try a new recipe I saw and cook something from scratch.",
        "I'm meeting a friend for coffee I haven't seen in months. And I've given myself permission to spend Saturday morning doing absolutely nothing productive.",
        "A small day trip I've planned. Nothing fancy but getting out of the usual environment will be good. Also hoping for some good weather so I can spend time outside.",
        "Finishing a book I've been reading. A gym session I know will feel great once I'm actually there. And some quiet evenings where I don't have to be anywhere or do anything.",
    ],
    "Describe a moment today when you felt at peace.": [
        "During my morning coffee before anyone else was awake. Just me, the warmth of the cup, and the quiet. No demands, no notifications, just being present.",
        "On my walk home when I noticed the light was really beautiful — golden and warm. I stopped for a minute and just took it in. Nothing else existed in that moment.",
        "In the middle of my meditation when my thoughts finally quieted. There were about five minutes where I felt completely still inside.",
        "When I was cooking dinner and listening to music. Completely absorbed in something simple and nourishing with no mental pressure.",
        "Reading before bed. The story pulled me completely into another world and all my worries just fell away for a while.",
    ],
    "What would you tell your past self from a year ago?": [
        "I would tell myself that the things I was so anxious about didn't turn out as catastrophically as I feared. That the hard period passed. That I was stronger than I knew and that asking for help was the right call.",
        "I'd tell myself to rest more and worry less. That grinding through exhaustion isn't a badge of honor. That the relationships I was neglecting mattered more than the things I was chasing.",
        "I would say: be kinder to yourself. You were doing your best with what you had. Stop waiting to feel ready — just start. And please, sleep more.",
        "I'd want to tell myself that it does get better, but not in the way you imagine. The circumstances don't magically change — you change. You learn to carry things differently.",
        "I'd tell myself to stop trying to have everything figured out. The uncertainty I was fighting so hard against turned out to be where the growth was.",
    ],
    "Let's work through an automatic negative thought that's been bothering you.": ANT_JOURNAL_CONTENT,
    "Identify a recurring negative thought pattern and challenge it.":            ANT_JOURNAL_CONTENT,
}

# ==================== USER PERSONAS ====================

USERS_DATA = [
    {
        "register": {"name": "Emma Chen", "email": "emma.chen@kind.com", "password": "EmmaKind1!"},
        "mood_pattern":       "anxious_improving",
        "mood_frequency":     0.88,
        "journal_frequency":  0.55,
        "habits": [
            {"name": "Morning meditation",  "description": "10 min mindfulness before work",  "colorHex": "#10B981", "iconIdentifier": "meditation", "completion": 0.70},
            {"name": "Exercise",            "description": "30 min workout",                  "colorHex": "#3B82F6", "iconIdentifier": "exercise",   "completion": 0.60},
            {"name": "Journaling",          "description": "Evening reflection",              "colorHex": "#8B5CF6", "iconIdentifier": "journal",    "completion": 0.65},
            {"name": "Limit social media",  "description": "Max 30 min per day",             "colorHex": "#F59E0B", "iconIdentifier": "phone",      "completion": 0.50},
        ],
    },
    {
        "register": {"name": "Marcus Johnson", "email": "marcus.johnson@kind.com", "password": "MarcusKind1!"},
        "mood_pattern":       "volatile",
        "mood_frequency":     0.75,
        "journal_frequency":  0.40,
        "habits": [
            {"name": "Study sessions",        "description": "2h focused study daily",          "colorHex": "#3B82F6", "iconIdentifier": "book",   "completion": 0.55},
            {"name": "Exercise",              "description": "Gym or outdoor run",              "colorHex": "#10B981", "iconIdentifier": "exercise","completion": 0.45},
            {"name": "Sleep by 11pm",         "description": "Consistent sleep schedule",       "colorHex": "#6366F1", "iconIdentifier": "moon",   "completion": 0.40},
            {"name": "No caffeine after 3pm", "description": "Better sleep quality",            "colorHex": "#F59E0B", "iconIdentifier": "coffee", "completion": 0.55},
        ],
    },
    {
        "register": {"name": "Sofia Rodriguez", "email": "sofia.rodriguez@kind.com", "password": "SofiaKind1!"},
        "mood_pattern":       "stable_medium",
        "mood_frequency":     0.85,
        "journal_frequency":  0.50,
        "habits": [
            {"name": "Self-care time",      "description": "30 min just for me",              "colorHex": "#EC4899", "iconIdentifier": "heart",   "completion": 0.65},
            {"name": "Rest when possible",  "description": "Nap when the baby naps",          "colorHex": "#6366F1", "iconIdentifier": "moon",    "completion": 0.55},
            {"name": "Gentle walk",         "description": "Fresh air with the baby",         "colorHex": "#10B981", "iconIdentifier": "walk",    "completion": 0.70},
            {"name": "Connect with partner","description": "Quality time each evening",        "colorHex": "#F59E0B", "iconIdentifier": "heart",   "completion": 0.60},
        ],
    },
    {
        "register": {"name": "Alex Thompson", "email": "alex.thompson@kind.com", "password": "AlexKind1!"},
        "mood_pattern":       "declining_then_improving",
        "mood_frequency":     0.80,
        "journal_frequency":  0.65,
        "habits": [
            {"name": "Morning run",        "description": "Outdoor 5k",                     "colorHex": "#10B981", "iconIdentifier": "run",      "completion": 0.60},
            {"name": "Social connection",  "description": "Reach out to one person",        "colorHex": "#3B82F6", "iconIdentifier": "people",   "completion": 0.50},
            {"name": "Journaling",         "description": "Process feelings",               "colorHex": "#8B5CF6", "iconIdentifier": "journal",  "completion": 0.70},
            {"name": "No alcohol",         "description": "Alcohol-free days",              "colorHex": "#EF4444", "iconIdentifier": "no-drink", "completion": 0.65},
            {"name": "Cook at home",       "description": "Nourishing meals",              "colorHex": "#F59E0B", "iconIdentifier": "food",     "completion": 0.55},
        ],
    },
    {
        "register": {"name": "Priya Patel", "email": "priya.patel@kind.com", "password": "PriyaKind1!"},
        "mood_pattern":       "burnout",
        "mood_frequency":     0.90,
        "journal_frequency":  0.60,
        "habits": [
            {"name": "Work cutoff at 7pm",   "description": "No work emails after 7pm",     "colorHex": "#EF4444", "iconIdentifier": "work",      "completion": 0.45},
            {"name": "Lunch break walk",     "description": "15 min outdoor break",          "colorHex": "#10B981", "iconIdentifier": "walk",      "completion": 0.60},
            {"name": "Evening meditation",   "description": "Wind-down mindfulness",         "colorHex": "#8B5CF6", "iconIdentifier": "meditation","completion": 0.65},
            {"name": "Weekly social plans",  "description": "One social activity per week",  "colorHex": "#3B82F6", "iconIdentifier": "people",    "completion": 0.70},
            {"name": "Yoga",                 "description": "Morning yoga routine",          "colorHex": "#EC4899", "iconIdentifier": "yoga",      "completion": 0.50},
        ],
    },
    {
        "register": {"name": "James Wilson", "email": "james.wilson@kind.com", "password": "JamesKind1!"},
        "mood_pattern":       "improving",
        "mood_frequency":     0.82,
        "journal_frequency":  0.55,
        "habits": [
            {"name": "Job applications",  "description": "Apply to 2 positions daily",   "colorHex": "#3B82F6", "iconIdentifier": "work",    "completion": 0.65},
            {"name": "Morning walk",      "description": "Get outside every day",         "colorHex": "#10B981", "iconIdentifier": "walk",    "completion": 0.75},
            {"name": "Skill building",    "description": "1h online course",              "colorHex": "#8B5CF6", "iconIdentifier": "book",    "completion": 0.55},
            {"name": "Connect with family","description": "Daily check-in call",          "colorHex": "#F59E0B", "iconIdentifier": "heart",   "completion": 0.70},
        ],
    },
    {
        "register": {"name": "Lily Park", "email": "lily.park@kind.com", "password": "LilyKind11!"},
        "mood_pattern":       "stable_high",
        "mood_frequency":     0.92,
        "journal_frequency":  0.70,
        "habits": [
            {"name": "Morning yoga",         "description": "20 min yoga flow",             "colorHex": "#EC4899", "iconIdentifier": "yoga",    "completion": 0.85},
            {"name": "Gratitude journaling", "description": "Write 3 things I'm grateful for","colorHex": "#F59E0B","iconIdentifier": "journal", "completion": 0.90},
            {"name": "Read 20 min",          "description": "Evening reading",               "colorHex": "#8B5CF6", "iconIdentifier": "book",    "completion": 0.80},
            {"name": "Hydration",            "description": "8 glasses of water",            "colorHex": "#3B82F6", "iconIdentifier": "water",   "completion": 0.75},
        ],
    },
    {
        "register": {"name": "David Brown", "email": "david.brown@kind.com", "password": "DavidKind1!"},
        "mood_pattern":       "seasonal_recovery",
        "mood_frequency":     0.78,
        "journal_frequency":  0.45,
        "habits": [
            {"name": "Light therapy",         "description": "Morning light lamp session",  "colorHex": "#F59E0B", "iconIdentifier": "sun",        "completion": 0.65},
            {"name": "Exercise",              "description": "Any physical activity",        "colorHex": "#10B981", "iconIdentifier": "exercise",   "completion": 0.50},
            {"name": "Social plans",          "description": "Schedule one social activity", "colorHex": "#3B82F6", "iconIdentifier": "people",     "completion": 0.45},
            {"name": "Vitamin D supplement",  "description": "Daily supplement",             "colorHex": "#F59E0B", "iconIdentifier": "supplement", "completion": 0.75},
        ],
    },
    {
        "register": {"name": "Aisha Mohammed", "email": "aisha.mohammed@kind.com", "password": "AishaKind1!"},
        "mood_pattern":       "stress_waves",
        "mood_frequency":     0.85,
        "journal_frequency":  0.60,
        "habits": [
            {"name": "Pomodoro study",     "description": "Focused study with timed breaks", "colorHex": "#EF4444", "iconIdentifier": "book",    "completion": 0.70},
            {"name": "Prayer & reflection","description": "Daily spiritual practice",         "colorHex": "#8B5CF6", "iconIdentifier": "prayer",  "completion": 0.85},
            {"name": "Exercise",           "description": "Gym session",                      "colorHex": "#10B981", "iconIdentifier": "exercise","completion": 0.55},
            {"name": "Call family",        "description": "Stay connected with home",         "colorHex": "#F59E0B", "iconIdentifier": "heart",   "completion": 0.75},
            {"name": "Consistent bedtime", "description": "Same sleep time every night",     "colorHex": "#6366F1", "iconIdentifier": "moon",    "completion": 0.50},
        ],
    },
    {
        "register": {"name": "Tom Martinez", "email": "tom.martinez@kind.com", "password": "TomKind11!"},
        "mood_pattern":       "performance_focused",
        "mood_frequency":     0.88,
        "journal_frequency":  0.45,
        "habits": [
            {"name": "Morning workout",       "description": "Strength training session",     "colorHex": "#EF4444", "iconIdentifier": "exercise","completion": 0.85},
            {"name": "Meditation",            "description": "Pre-performance mindfulness",   "colorHex": "#8B5CF6", "iconIdentifier": "meditation","completion": 0.70},
            {"name": "Nutrition tracking",    "description": "Log all meals",                 "colorHex": "#10B981", "iconIdentifier": "food",    "completion": 0.75},
            {"name": "Sleep 8 hours",         "description": "Prioritize recovery sleep",     "colorHex": "#6366F1", "iconIdentifier": "moon",    "completion": 0.65},
            {"name": "Evening stretch",       "description": "Full body stretch routine",     "colorHex": "#F59E0B", "iconIdentifier": "stretch", "completion": 0.60},
        ],
    },
]

# ==================== BREATHING SESSION GENERATION ====================

BREATHING_EXERCISE_TYPES = ["box", "478", "calm"]
BREATHING_FREQUENCY = 0.35  # baseline chance of a logged breathing session on a given day

# ==================== MOOD VALUE GENERATION ====================

def mood_value_for_pattern(pattern: str, day_idx: int, total: int) -> int:
    t = day_idx / max(total - 1, 1)
    noise = random.gauss(0, 1.1)
    # Slight weekend boost (approximated from index)
    weekend_boost = 0.5 if day_idx % 7 in (5, 6) else 0.0

    if pattern == "improving":
        base = 3.5 + t * 4.5

    elif pattern == "declining_then_improving":
        # Starts ~7, crashes to ~3 at midpoint, recovers to ~6-7
        if t < 0.45:
            base = 7.0 - t * 8.0
        else:
            base = 3.5 + (t - 0.45) * 6.5

    elif pattern == "stable_high":
        base = 7.5

    elif pattern == "stable_medium":
        base = 5.5

    elif pattern == "volatile":
        base = 5.0 + random.gauss(0, 1.8)

    elif pattern == "burnout":
        # Starts high, crashes sharply in middle, slow recovery
        if t < 0.20:
            base = 7.5 - t * 10.0
        elif t < 0.55:
            base = 3.0 + random.gauss(0, 0.6)
        else:
            base = 3.5 + (t - 0.55) * 8.0

    elif pattern == "anxious_improving":
        base = 3.0 + t * 5.0
        if t < 0.4:
            noise = random.gauss(0, 1.8)  # more noise early on

    elif pattern == "seasonal_recovery":
        base = 3.0 + t * 5.5
        if random.random() < 0.12:  # occasional rough patch
            base -= 2.0

    elif pattern == "stress_waves":
        # Periodic dips simulating exam/deadline cycles
        base = 5.5 + 1.5 * math.sin(t * 3 * math.pi) + t * 1.5

    elif pattern == "performance_focused":
        base = 6.5 + 0.5 * t
        if random.random() < 0.08:  # competition anxiety spikes
            base -= 2.5

    else:
        base = 5.5

    return max(1, min(10, round(base + noise + weekend_boost)))


def mood_category_for_value(v: int) -> str:
    if v <= 2: return "VERY_UNPLEASANT"
    if v <= 4: return "UNPLEASANT"
    if v <= 6: return "NEUTRAL"
    if v <= 8: return "PLEASANT"
    return "VERY_PLEASANT"


def pick_emotions(emotions_by_category: dict, mood_val: int) -> list:
    cat = mood_category_for_value(mood_val)
    pool = emotions_by_category.get(cat, [])
    if not pool:
        return []
    count = random.randint(1, min(3, len(pool)))
    return random.sample(pool, count)


def pick_factors(all_factors: list, count: int = None) -> list:
    if not all_factors:
        return []
    n = count if count else random.randint(1, min(3, len(all_factors)))
    return random.sample(all_factors, n)


def pick_mood_note(mood_val: int) -> str | None:
    if random.random() > 0.40:
        return None
    notes = {
        (1, 2):  ["Really struggling today.", "Can't seem to shake this feeling.", "Everything feels heavy."],
        (3, 4):  ["Tough day but getting through it.", "Anxious today. Tried breathing exercises.", "Tired and overwhelmed."],
        (5, 6):  ["Okay day. Nothing special.", "Steady. Taking it one step at a time.", "Calm, just moving through the day."],
        (7, 8):  ["Good energy today!", "Productive and feeling connected.", "Grateful for today."],
        (9, 10): ["Amazing day!", "Feeling incredible!", "So grateful and happy right now."],
    }
    for (lo, hi), choices in notes.items():
        if lo <= mood_val <= hi:
            return random.choice(choices)
    return None


def pick_journal_blank(mood_val: int) -> tuple:
    if mood_val <= 2:   key = "low"
    elif mood_val <= 4: key = "medium_low"
    elif mood_val <= 6: key = "medium"
    elif mood_val <= 8: key = "medium_high"
    else:               key = "high"
    return random.choice(JOURNAL_BLANK[key])


# ==================== HTTP HELPERS ====================

def _headers(token: str | None = None) -> dict:
    h = {"Content-Type": "application/json"}
    if token:
        h["Authorization"] = f"Bearer {token}"
    return h


def api_post(path: str, data: dict, token: str | None = None):
    try:
        r = requests.post(f"{BASE_URL}{path}", json=data, headers=_headers(token), timeout=8)
        if r.status_code in (200, 201):
            try:
                return r.json()
            except Exception:
                return {}
        # silent on duplicates / conflicts
        if r.status_code not in (400, 409):
            print(f"  WARN POST {path} -> {r.status_code}: {r.text[:150]}")
        return None
    except Exception as e:
        print(f"  ERROR POST {path}: {e}")
        return None


def api_get(path: str, token: str | None = None):
    try:
        r = requests.get(f"{BASE_URL}{path}", headers=_headers(token), timeout=10)
        if r.status_code == 200:
            return r.json()
        return None
    except Exception as e:
        print(f"  ERROR GET {path}: {e}")
        return None


def api_patch(path: str, token: str | None = None):
    try:
        requests.patch(f"{BASE_URL}{path}", headers=_headers(token), timeout=10)
    except Exception:
        pass


def api_post_habit_log(habit_id: int, date_str: str, completed: bool, token: str):
    try:
        r = requests.post(
            f"{BASE_URL}/api/habits/{habit_id}/logs/date/{date_str}",
            params={"completed": str(completed).lower()},
            headers=_headers(token),
            timeout=8,
        )
        if r.status_code in (200, 201):
            return True
        print(f"  WARN HABIT LOG habit={habit_id} date={date_str} -> {r.status_code}: {r.text[:120]}")
        return False
    except Exception as e:
        print(f"  ERROR HABIT LOG habit={habit_id} date={date_str}: {e}")
        return False


# ==================== REFERENCE DATA SEEDING ====================

def get_seed_token() -> str | None:
    """Register (or login) a throwaway seed user and return their JWT token.
    This token is used only to create shared reference data that requires auth."""
    creds = {"name": "Seed Admin", "email": "seed.admin@kind.com", "password": "SeedAdmin1!"}
    result = api_post("/api/register", creds)
    if result and result.get("token"):
        return result["token"]
    # Already registered — log in
    result = api_post("/api/login", {"email": creds["email"], "password": creds["password"]})
    return result.get("token") if result else None


def seed_reference_data(token: str):
    """Seed or fetch emotions, mood factors, journal prompts, and init system data."""
    print("\n=== Seeding reference data ===")

    # System init (idempotent endpoints)
    api_post("/api/affirmations/init", {}, token)
    api_post("/api/exercises/initialize", {}, token)
    print("  System data initialized (affirmations, exercises)")

    # --- Emotions ---
    existing = api_get("/api/emotions", token) or []
    emotions_by_category: dict[str, list] = {}
    if existing:
        print(f"  Found {len(existing)} existing emotions — reusing")
        for em in existing:
            cat = em.get("moodCategory", "NEUTRAL")
            emotions_by_category.setdefault(cat, []).append({"id": em["id"]})
    else:
        print("  Creating emotions…")
        for em in EMOTIONS_DATA:
            result = api_post("/api/emotions", em, token)
            if result:
                cat = result.get("moodCategory", em["moodCategory"])
                emotions_by_category.setdefault(cat, []).append({"id": result["id"]})
        print(f"    {sum(len(v) for v in emotions_by_category.values())} emotions created")

    # --- Mood Factors ---
    existing = api_get("/api/mood-factors", token) or []
    all_factors: list[dict] = []
    if existing:
        print(f"  Found {len(existing)} existing mood factors — reusing")
        all_factors = [{"id": mf["id"]} for mf in existing]
    else:
        print("  Creating mood factors…")
        for mf in MOOD_FACTORS_DATA:
            result = api_post("/api/mood-factors", mf, token)
            if result:
                all_factors.append({"id": result["id"]})
        print(f"    {len(all_factors)} mood factors created")

    # --- Journal Prompts ---
    existing = api_get("/api/journal-prompts", token) or []
    prompt_ids: list[int] = []
    prompts_by_text: dict[str, int] = {}
    ant_prompt_ids: list[int] = []
    general_prompts: list[tuple[str, int]] = []

    if existing:
        print(f"  Found {len(existing)} existing journal prompts — reusing")
        for jp in existing:
            pid = jp["id"]
            text = jp.get("promptText", "")
            prompt_ids.append(pid)
            prompts_by_text[text] = pid
            if jp.get("type") == "ANT_EXERCISE":
                ant_prompt_ids.append(pid)
            else:
                general_prompts.append((text, pid))
    else:
        print("  Creating journal prompts…")
        for jp in JOURNAL_PROMPTS_DATA:
            result = api_post("/api/journal-prompts", jp, token)
            if result:
                pid = result["id"]
                text = jp["promptText"]
                prompt_ids.append(pid)
                prompts_by_text[text] = pid
                if jp["type"] == "ANT_EXERCISE":
                    ant_prompt_ids.append(pid)
                else:
                    general_prompts.append((text, pid))
        print(f"    {len(prompt_ids)} journal prompts created")

    print(f"  Reference data ready: {sum(len(v) for v in emotions_by_category.values())} emotions, "
          f"{len(all_factors)} factors, {len(prompt_ids)} prompts")

    return emotions_by_category, all_factors, ant_prompt_ids, general_prompts


# ==================== PER-USER SEEDING ====================

def seed_user(user_data: dict, emotions_by_category: dict, all_factors: list,
              ant_prompt_ids: list, general_prompts: list,
              start_date: date, end_date: date):
    reg = user_data["register"]
    name = reg["name"]
    email = reg["email"]
    print(f"\n--- {name} ({email}) ---")

    # Register
    result = api_post("/api/register", reg)
    token = None
    if result:
        token = result.get("token")

    if not token:
        # May already exist — try login
        result = api_post("/api/login", {"email": email, "password": reg["password"]})
        if result:
            token = result.get("token")

    if not token:
        print(f"  FAILED to authenticate — skipping user")
        return

    # Fetch user ID
    user_info = api_get(f"/api/users/email/{email}", token)
    if not user_info:
        print(f"  FAILED to retrieve user object — skipping")
        return

    user_id: int = user_info["id"]
    print(f"  user_id={user_id}  token=...{token[-8:]}")

    # Mark intro complete
    api_patch(f"/api/users/{user_id}/complete-intro", token)

    # Create habits
    habit_specs = []  # [(habit_id, completion_rate)]
    for h in user_data["habits"]:
        payload = {
            "name":            h["name"],
            "description":     h["description"],
            "colorHex":        h["colorHex"],
            "iconIdentifier":  h["iconIdentifier"],
            "creationDate":    str(start_date),
            "isSystemHabit":   False,
        }
        result = api_post(f"/api/habits/user/{user_id}", payload, token)
        if result:
            habit_specs.append((result["id"], h["completion"]))
    print(f"  Created {len(habit_specs)} habits")

    total_days = (end_date - start_date).days + 1
    mood_count = journal_count = log_count = breathing_count = 0

    for day_idx in range(total_days):
        current = start_date + timedelta(days=day_idx)
        date_str = str(current)

        # ---- Mood entry ----
        if random.random() < user_data["mood_frequency"]:
            mood_val = mood_value_for_pattern(
                user_data["mood_pattern"], day_idx, total_days
            )
            mood_payload = {
                "date":              date_str,
                "moodValue":         mood_val,
                "note":              pick_mood_note(mood_val),
                "user":              {"id": user_id},
                "selectedEmotions":  pick_emotions(emotions_by_category, mood_val),
                "selectedFactors":   pick_factors(all_factors),
            }
            mood_result = api_post("/api/mood-entries", mood_payload, token)

            if mood_result:
                mood_count += 1

            # ---- Journal entry ----
            # Independent of whether the mood post above succeeded — a failed/rejected
            # mood entry shouldn't also cost that day's journal entry.
            if random.random() < user_data["journal_frequency"]:
                use_prompt = random.random() < 0.35

                if use_prompt and (general_prompts or ant_prompt_ids):
                    # ANT exercise: 25% chance when mood is low
                    use_ant = (
                        random.random() < 0.25
                        and mood_val <= 5
                        and ant_prompt_ids
                    )
                    if use_ant:
                        prompt_id = random.choice(ant_prompt_ids)
                        content = random.choice(ANT_JOURNAL_CONTENT)
                    else:
                        if general_prompts:
                            prompt_text, prompt_id = random.choice(general_prompts)
                            options = PROMPT_JOURNAL_CONTENT.get(prompt_text, [])
                            if isinstance(options, list) and options:
                                chosen = random.choice(options)
                                content = chosen if isinstance(chosen, str) else random.choice(ANT_JOURNAL_CONTENT)
                            else:
                                content = "Reflecting on today's prompt."
                        else:
                            prompt_id = ant_prompt_ids[0]
                            content = random.choice(ANT_JOURNAL_CONTENT)

                    journal_payload = {
                        "createdAt":     date_str,
                        "title":         None,
                        "content":       content,
                        "type":          "PROMPT_BASED",
                        "user":          {"id": user_id},
                        "journalPrompt": {"id": prompt_id},
                    }
                else:
                    title, content = pick_journal_blank(mood_val)
                    journal_payload = {
                        "createdAt": date_str,
                        "title":     title,
                        "content":   content,
                        "type":      "BLANK",
                        "user":      {"id": user_id},
                    }

                if api_post("/api/journal-entries", journal_payload, token):
                    journal_count += 1

        # ---- Breathing session ----
        if random.random() < BREATHING_FREQUENCY:
            breathing_payload = {
                "date":         date_str,
                "exerciseType": random.choice(BREATHING_EXERCISE_TYPES),
                "cycles":       random.randint(2, 8),
                "user":         {"id": user_id},
            }
            if api_post("/api/breathing-sessions", breathing_payload, token):
                breathing_count += 1

        # ---- Habit daily logs ----
        for habit_id, completion_rate in habit_specs:
            completed = random.random() < completion_rate
            if api_post_habit_log(habit_id, date_str, completed, token):
                log_count += 1

        if (day_idx + 1) % 30 == 0:
            print(f"    Day {day_idx+1}/{total_days}: "
                  f"{mood_count} moods | {journal_count} journals | "
                  f"{breathing_count} breathing sessions | {log_count} habit logs")

    print(f"  DONE — {mood_count} mood entries, {journal_count} journal entries, "
          f"{breathing_count} breathing sessions, {log_count} habit logs")


# ==================== MAIN ====================

def main():
    print("=" * 55)
    print("   Kind Mental Health App — Database Seeder")
    print("=" * 55)
    print(f"Backend : {BASE_URL}")
    print(f"Users   : {len(USERS_DATA)}")

    end_date   = date.today() - timedelta(days=1)
    start_date = end_date - timedelta(days=119)   # ~4 months
    print(f"Period  : {start_date}  →  {end_date}  ({(end_date - start_date).days + 1} days)")
    print()

    # Connectivity check (401 is fine — server is up, just requires auth)
    try:
        r = requests.get(f"{BASE_URL}/api/affirmations", timeout=5)
        print(f"Backend reachable (HTTP {r.status_code})")
    except Exception as e:
        print(f"ERROR: Cannot reach backend at {BASE_URL}")
        print(f"       {e}")
        print("       Make sure the Spring Boot app is running first.")
        sys.exit(1)

    # Get a token for seeding shared reference data
    print("Acquiring seed token…")
    seed_token = get_seed_token()
    if not seed_token:
        print("ERROR: Could not register or login the seed admin user — aborting.")
        sys.exit(1)
    print(f"Seed token acquired (...{seed_token[-8:]})")

    emotions_by_category, all_factors, ant_prompt_ids, general_prompts = seed_reference_data(seed_token)

    print(f"\nSeeding {len(USERS_DATA)} users in parallel (4 at a time)…\n")

    def seed_one(user_data):
        seed_user(
            user_data,
            emotions_by_category,
            all_factors,
            ant_prompt_ids,
            general_prompts,
            start_date,
            end_date,
        )

    with ThreadPoolExecutor(max_workers=4) as executor:
        futures = {executor.submit(seed_one, u): u["register"]["name"] for u in USERS_DATA}
        for future in as_completed(futures):
            name = futures[future]
            try:
                future.result()
            except Exception as e:
                print(f"  ERROR seeding {name}: {e}")

    print()
    print("=" * 55)
    print("   Seeding complete!")
    print("=" * 55)


if __name__ == "__main__":
    main()
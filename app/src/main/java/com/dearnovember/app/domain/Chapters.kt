package com.dearnovember.app.domain

data class ChapterContent(val day: Int, val title: String, val quote: String, val mission: String, val question: String)

object Chapters {
    val all: List<ChapterContent> = listOf(
        ChapterContent(1, "The Beginning", "Every version of you begins with a decision.", "Complete today's tasks even if you don't feel motivated.", "What am I promising myself this month?"),
        ChapterContent(2, "Meet Your Old Self", "You can't leave a person behind until you've looked at them honestly.", "Write down three habits you want to outgrow, without judging them.", "What did my old self protect me from?"),
        ChapterContent(3, "The First Promise", "A small promise kept is worth more than a big one announced.", "Pick one promise for today and keep it exactly.", "Which promise am I most likely to break, and why?"),
        ChapterContent(4, "Breaking the Cycle", "Patterns loosen when you interrupt them once, then again.", "Notice one moment you'd usually procrastinate. Start for just five minutes.", "What usually happens right before I give in?"),
        ChapterContent(5, "Discipline Over Motivation", "Motivation is weather. Discipline is the roof.", "Do your hardest task first, before checking your phone.", "What did I do today that I didn't feel like doing?"),
        ChapterContent(6, "The Battle With Distraction", "Attention is the one thing you can only spend, never save.", "Spend 10 minutes without your phone, fully present.", "What pulls my attention most, and what is it really offering?"),
        ChapterContent(7, "Seven Days Strong", "A week ago this was an idea. Now it's a record.", "Look back at Day 1. Name one thing that is already easier.", "What has surprised me about myself this week?"),
        ChapterContent(8, "A Stronger Body", "Strength is built in the quiet reps nobody applauds.", "Do your workout with clean form. Stop before pain, not after.", "How does my body feel when I treat it well?"),
        ChapterContent(9, "Feed Your Mind", "What you read, hear and repeat becomes the room you live in.", "Learn one new concept today and explain it in your own words.", "What am I feeding my mind lately?"),
        ChapterContent(10, "The Battle With Your Old Habits", "Old habits knock politely. You don't have to open the door.", "Spot one old habit today and choose a different response.", "Which old habit showed up, and what did I do?"),
        ChapterContent(11, "Speak Without Fear", "Clear beats perfect. A calm voice is made by practice, not pretence.", "Speak for two minutes on any topic, slowly and clearly.", "Where did my voice feel steady today?"),
        ChapterContent(12, "Stop Comparing", "Someone else's highlight reel was never your starting line.", "Go one full day without measuring yourself against anyone.", "Whose pace was I trying to match, and why?"),
        ChapterContent(13, "The Art of Focus", "Depth is a decision you make again every few minutes.", "Finish one 45-minute focus session with a single goal.", "What helped me stay on one thing?"),
        ChapterContent(14, "Two Weeks of Progress", "Halfway is where most people quietly stop. You're still here.", "Review your scores so far. Fix one thing, keep one thing.", "What is working that I should protect?"),
        ChapterContent(15, "Becoming Comfortable With Yourself", "Being alone with your thoughts gets quieter when you stop fighting them.", "Sit for ten minutes with no input. Just notice.", "What do I like about my own company?"),
        ChapterContent(16, "Emotional Independence", "You can care deeply and still stand on your own two feet.", "Complete one task without seeking anyone's approval.", "What do I do when I want reassurance, and what else could I do?"),
        ChapterContent(17, "Respect Your Own Worth", "Self-respect sounds like keeping your word to yourself.", "Practise saying no kindly to one thing that drains you.", "Where did I put my needs last today?"),
        ChapterContent(18, "The Mind Is Your Strength", "Thoughts are visitors. Facts are the ones who stay.", "When a worry appears, ask: fact or assumption?", "Which worry turned out to be an assumption?"),
        ChapterContent(19, "Consistency Is Power", "Ordinary days, repeated, make extraordinary people.", "Do the basics well today. Nothing flashy.", "What small thing have I done every day?"),
        ChapterContent(20, "Becoming More Confident", "Confidence is evidence you've collected by showing up.", "Do one thing slightly outside your comfort zone.", "What proof do I have that I can do hard things?"),
        ChapterContent(21, "Three Weeks Strong", "Twenty-one days ago you only had a plan. Now you have proof.", "Compare today with Day 1. Write down every difference.", "Who am I becoming?"),
        ChapterContent(22, "The Final Stretch", "The end rewards the people who stay boring and steady.", "Finish pending work. Leave nothing half-done.", "What do I want finished by the 30th?"),
        ChapterContent(23, "Keep Your Promise", "You said you'd become someone you're proud of. Keep going.", "Do today's tasks as if the first day's you were watching.", "What promise matters most to keep now?"),
        ChapterContent(24, "Dear November", "Thank you for the version of me you helped me meet.", "Take your final photo, review your journey, and write yourself a letter.", "What will I carry from November into December?"),
    )
    fun forDay(day: Int) = all[(day - 1).coerceIn(0, all.lastIndex)]
}

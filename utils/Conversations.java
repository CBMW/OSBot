package utils;

import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.script.Script;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

/**
 * Handles chat messages and responds based on predefined patterns, while also
 * detecting and handling trade invitation messages.
 */
public class Conversations {

    private static final Map<String, String[]> regexResponses = new LinkedHashMap<>();
    private static final Map<String, Long> recentResponses = new HashMap<>();
    private static final long RESPONSE_COOLDOWN = 10_000; // 10 seconds cooldown per user
    private static final Map<String, String[]> conversationBank = new HashMap<>();
    
    static {
        conversationBank.put("greetings", new String[]{
    	    "Hi there", "Hello", "Hey", "Yo", "Sup", "Howdy", "Greetings", 
    	    "Heyo", "Hail", "Hiya", "Hola", "Hey hey", "Wassup", "Hey dude", 
    	    "Hey mate", "Sup bro", "Yo yo", "G'day", "Heya", "Hi", 
    	    "What's up", "Salutations", "Oi", "Ello", "Hey fam", 
    	    "Yo fam", "Sup homie", "Hi friend", "Yo gamer", "Sup gamer", 
    	    "Heyo gamer", "Yo adventurer", "Howdy mate", "Hey buddy", 
    	    "Yo buddy", "Hi there mate", "Hey pal", "Wassup dude", 
    	    "Yo partner", "Hey champ", "Hey chief", "Hey warrior", 
    	    "Hey player", "Hey traveler", "Sup adventurer", "Yo explorer", 
    	    "Hi champ", "Yo friend", "Howdy adventurer", "Hey adventurer", 
    	    "Hey explorer", "Hey friend", "Hi there gamer", "Yo homie", 
    	    "Hey there", "Sup there", "What�s good", "Yo there", "Hi adventurer", 
    	    "Hi explorer", "Greetings traveler", "Hey buddy", "Hey there mate", 
    	    "Yo friend", "Hi pal", "Ello adventurer", "Hey pal", "Sup mate", 
    	    "Howdy traveler", "Heyo pal", "Yo mate", "Hi hi", "Hey gamer", 
    	    "What up", "What�s crackin", "Sup traveler", "Hey traveler", 
    	    "Yo champ", "Hey you", "Yo partner", "Sup partner", "Greetings mate", 
    	    "Hi ya", "Ello mate", "Yo adventurer", "Howdy explorer", 
    	    "Sup warrior", "Greetings adventurer", "Hey pal", "Hi there champ", 
    	    "What�s up gamer", "Yo dude", "What�s up pal", "Ello gamer", 
    	    "Sup buddy", "Hi traveler", "Hey champ", "Howdy gamer", 
    	    "Hey you there", "Yo pal", "Greetings bro", "Hey partner"
    	});
    }

        static {
            conversationBank.put("greetingResponses", new String[]{
    	    "I'm doing well, thanks", "Good, how about you", "Just hanging out here", "Not bad, how's your day",
    	    "Could be better, just saving GP", "Fine, just looking around", "Doing okay, thanks for asking",
    	    "Pretty good, how about you", "Alright, just chilling", "Good enough, what about you", "Doing fine",
    	    "All good here", "Not too bad", "Hanging in there", "Can't complain", "Just relaxing", "Okay, how about you",
    	    "Fairly decent", "Doing alright", "Doing fine, just grinding", "Not too bad, just vibing", "Could be worse",
    	    "Just taking it easy", "All good, thanks", "I'm okay, how about yourself", "Keeping busy", "Decent, what's up with you",
    	    "Doing okay, just wandering", "Good enough", "Same old, same old", "Pretty chill here", "Not bad at all",
    	    "Fine, just adventuring", "Doing alright, just here", "Meh, it�s alright", "Pretty decent", "Chillin�, you",
    	    "Not terrible", "Doing well, just exploring", "Decent day so far", "It�s alright", "Can�t complain, just hanging out",
    	    "Same as usual", "Okay for now", "Doing good, thanks", "Just here, you know", "Not bad, how are you",
    	    "Pretty good, just looking around", "Doing fine, how about you", "Good enough, yourself", "Just out here",
    	    "Hanging around", "Good, just trying to make progress", "Okay, just here", "Doing okay, you", "Alright, thanks for asking",
    	    "It�s been better, you", "Not much going on", "All good, what about you", "Fine, just trying to save up GP",
    	    "Pretty good so far", "Good day so far", "It�s going okay", "Doing alright for now", "Doing fine, thanks",
    	    "Same here, just chilling", "Good enough day", "Hanging out for now", "Alright, thanks", "Not bad at all, thanks",
    	    "Good enough for me", "Doing good, how are you", "It�s a good day", "Can�t complain much", "Just passing time",
    	    "Decent so far", "Doing alright, thanks", "It�s fine", "Okay, what about you", "Just alright", "Pretty okay",
    	    "Fine for now", "Not much, just hanging", "Doing okay, you?", "It�s decent", "All good, you?", "Doing okay for now",
    	    "Fine enough", "Doing fine, how�s your day", "Good, thanks for asking", "It�s alright, thanks", "Not bad so far",
    	    "Pretty alright", "Doing good for now", "Just taking it slow", "Chillin', how about you"
    	});
   }
        static {
            conversationBank.put("compliments", new String[]{
    	    "Nice gear", "Cool outfit", "You look great", "Wow, you're strong", "Love your setup", 
    	    "That looks awesome", "You�re really impressive", "You�re so skilled", "Looking sharp", 
    	    "You look ready for anything", "Awesome style", "That�s some cool stuff", "You�ve got great gear",
    	    "Your setup is on point", "Looking powerful", "You�re well-prepared", "Your style stands out",
    	    "You�re a real pro", "Great choice of gear", "You�re rocking it", "That�s an impressive setup",
    	    "You look like a champion", "You�re decked out", "Wow, you�re prepared", "You�re geared up nicely",
    	    "You�ve got style", "Looking like a pro", "Your gear is top-notch", "You look unstoppable",
    	    "You�re rocking that look", "Your equipment is impressive", "That�s a solid setup", 
    	    "You�re ready for action", "You�ve got a great loadout", "You look strong", "You�re looking sharp today",
    	    "Your setup is amazing", "You look like a legend", "You�re ready for anything", "You�ve got an awesome vibe",
    	    "Looking like a real hero", "Your loadout is great", "You look like a pro adventurer", "That�s a killer look",
    	    "You�re all set", "You�re really prepared", "You�re looking like a boss", "You�ve got a fantastic setup",
    	    "That�s some serious gear", "You�ve got a cool look", "You�re looking pretty strong", "You�re a force to be reckoned with",
    	    "Your style is awesome", "You�ve got a winning setup", "You look like you mean business", "That�s a solid look",
    	    "You�ve got such cool gear", "You�re looking really skilled", "You�ve got a fantastic loadout", "You look amazing",
    	    "That�s some stylish gear", "You�re a master adventurer", "You�ve got an epic look", "Your setup is flawless",
    	    "You�ve got a really cool style", "You�re rocking a great look", "Your gear is really solid", "You look like a pro",
    	    "You�re totally prepared", "That�s some sleek gear", "You�re a true adventurer", "You�ve got an incredible setup",
    	    "You�re rocking that gear", "That�s a sharp look", "You�re decked out really well", "Your style is unbeatable",
    	    "You�re looking like a legend", "You�ve got a great sense of style", "You�re looking fierce", 
    	    "You�re rocking that setup", "Your loadout is fantastic", "You�ve got such a strong look", 
    	    "Your setup is next level", "You�ve got great taste in gear", "You look really confident", 
    	    "That�s a standout look", "Your equipment is spot on", "You�re a total pro", "You�re a legend in the making", 
    	    "Your look is flawless", "You�re an inspiration", "You�ve got that perfect setup", "You�re ready for anything"
    	});}
        
        static {
            conversationBank.put("askForGp", new String[]{
    	    "Could you spare some gp", "Any spare gp would help me", "I'm new, could you donate gp",
    	    "Trying to get started, any gp would be amazing", "Just need a little gp boost",
    	    "Could you help me with a bit of gp", "Any donations would mean the world to me",
    	    "Even 1k gp would be appreciated", "Do you have any gp to spare", "Just trying to get started, anything helps",
    	    "Anything you can spare would be awesome", "Struggling to get started, any gp helps",
    	    "Any kind players willing to donate gp", "Even a small donation would help me a lot",
    	    "Starting out is hard, any gp would be amazing", "A little gp goes a long way for me",
    	    "Trying to buy gear, need a bit of gp", "Every bit of gp helps, could you spare some",
    	    "Please help me get started, any gp is appreciated", "Just looking for a small boost, any gp helps",
    	    "Trying to save up, could you donate some gp", "Even 500gp would help me right now",
    	    "Could you donate a bit of gp to help a noob", "I�d appreciate any spare gp you can give",
    	    "Any kind soul willing to help me out with gp", "Any gp you don�t need would mean the world to me",
    	    "Struggling to get by, any gp would help", "Do you have any spare gp for a struggling adventurer",
    	    "Even a little gp would make my day", "Trying to progress, any gp donations would help",
    	    "Could you donate gp to help me train", "Looking for a kind player to help with gp",
    	    "Please consider donating some gp to help me out", "Even 1k gp would go a long way for me",
    	    "Just need a little push, could you spare some gp", "Every donation helps, even the smallest amount",
    	    "Any help at all would mean so much to me", "Do you have any gp you could spare",
    	    "Trying to get on my feet, any gp would be appreciated", "Anything at all helps, even the smallest donation",
    	    "Could you help a new player with a bit of gp", "Every little bit counts, could you donate some gp",
    	    "Please spare any gp you don�t need", "Trying to buy gear, need a little gp",
    	    "Any gp you could spare would make my day", "Looking for help from kind players, any gp helps",
    	    "Starting from scratch, any gp would be appreciated", "Even a tiny amount of gp would help me",
    	    "Could you help me out with some spare gp", "Struggling to progress, could you spare some gp",
    	    "Any gp you can spare would mean so much to me", "Trying to save up, could you help with gp",
    	    "Any donations would go a long way for me", "Just looking for a bit of help, any gp helps",
    	    "Please consider donating to help me out", "Even 1k gp would mean a lot to me",
    	    "Do you have any spare gp for someone starting out", "Just trying to get started, any amount helps",
    	    "Could you help me out with even a small donation", "Any help at all would mean the world to me",
    	    "Starting out is tough, any gp would be amazing", "Any kind players willing to help me out",
    	    "Even a little gp would make a big difference for me", "Trying to progress, any donations help",
    	    "Could you spare a little gp to help me out", "Every donation counts, even the smallest amount",
    	    "Looking for help, any gp donations would mean so much", "Starting from nothing, any gp helps",
    	    "Please help me out, even a tiny amount of gp helps", "Do you have any gp you don�t need",
    	    "Trying to get by, could you spare some gp", "Every little bit of gp makes a difference",
    	    "Any donations at all would help me so much", "Could you donate a little gp to help me",
    	    "Trying to save up for gear, any gp helps", "Do you have any gp you can spare for me",
    	    "Looking for kind players to help me out with gp", "Starting from scratch, anything helps",
    	    "Please consider helping me out with a small donation", "Any gp donations would mean the world to me",
    	    "Trying to progress, could you spare a little gp", "Even 500gp would make a big difference for me",
    	    "Could you help a new adventurer with some gp", "Every bit of gp helps me get closer to my goals",
    	    "Struggling to get by, could you help with some gp", "Do you have any spare gp to help me out",
    	    "Any help at all would mean so much to me", "Trying to save up, any donations help",
    	    "Please consider donating a little gp to help me out", "Even 1k gp would go a long way for me",
    	    "Do you have any gp you could spare for a noob", "Starting from nothing, any donations help",
    	    "Any donations would mean the world to me", "Looking for help, any gp donations help",
    	    "Could you spare a little gp to help me out", "Every donation counts, even the smallest one",
    	    "Looking for kind players to help with gp", "Please spare any gp you can for me",
    	    "Trying to get on my feet, any gp would mean so much", "Every bit of gp helps, could you donate some",
    	    "Looking for help, any donations mean the world to me", "Do you have any gp to spare",
    	    "Trying to progress, could you help with some gp", "Every little bit of gp counts, please help",
    	    "Any spare gp would be amazing, please help me", "Starting out is tough, any gp donations help",
    	    "Please help me get started, even a small donation helps", "Any gp you can spare would mean so much",
    	    "Trying to get by, do you have any spare gp", "Every donation helps, even a tiny one",
    	    "Do you have any gp you don�t need", "Looking for help, any gp donations mean so much",
    	    "Please help me out with a bit of gp", "Any donations would go a long way for me",
    	    "Starting out is hard, could you help with some gp", "Every little bit of gp makes a difference for me",
    	    "Any help at all would mean so much to me", "Trying to get started, could you help with gp",
    	    "Do you have any gp you could spare for me", "Any kind players willing to help with gp",
    	    "Looking for help, any donations would mean the world", "Could you spare some gp for me",
    	    "Even 1k gp would be amazing, please help", "Trying to progress, any gp donations help",
    	    "Looking for kind players to help me out", "Please spare any gp you don�t need",
    	    "Every little bit of gp counts, could you help", "Do you have any gp you don�t need",
    	    "Trying to save up, could you donate some gp", "Looking for help, any donations help me a lot",
    	    "Even 500gp would make a big difference for me", "Do you have any gp you can spare",
    	    "Looking for kind players to help me with gp", "Please help me get started with a bit of gp",
    	    "Any donations would be so appreciated", "Do you have any gp you could spare for a noob",
    	    "Any gp you can spare would help me so much", "Trying to get started, could you help me with gp",
    	    "Do you have any gp you can spare for me", "Please consider donating a little gp to help me",
    	    "Even a tiny donation would mean a lot to me", "Every little bit of gp helps me so much",
    	    "Do you have any gp you don�t need", "Trying to get on my feet, any gp donations help",
    	    "Every donation counts, even the smallest one", "Please help me out with some gp",
    	    "Looking for help, any gp donations mean the world", "Do you have any gp you can spare for me",
    	    "Starting from scratch, could you help with gp", "Any donations would mean so much to me",
    	    "Looking for help, any donations help a lot", "Do you have any gp you don�t need",
    	    "Trying to progress, any donations mean so much", "Looking for kind players to help with gp",
    	    "Please consider donating some gp to help me out", "Do you have any gp you can spare for a noob",
    	    "Trying to get started, could you help with some gp", "Any gp donations would mean so much to me"
            });}

        static {
            conversationBank.put("thanks", new String[]{
    	    "Thank you so much", "You're awesome, thanks", "Really appreciate it", "Thanks a ton", "You're too kind",
    	    "Thanks a lot, that means so much", "Thank you, you're amazing", "Big thanks for helping me out",
    	    "Cheers, you're a legend", "Couldn't thank you enough",
    	    "Thanks, you're the best", "Wow, thanks a million", "Thanks a bunch", "You made my day, thanks",
    	    "Grateful for your kindness, thank you", "Massive thanks", "Huge thanks, you're incredible",
    	    "Appreciate it so much", "You're a star, thanks", "Thanks a ton, seriously",
    	    "So grateful, thank you", "Thanks for helping me out", "Thanks for being so kind",
    	    "You're amazing, thanks a lot", "Couldn't have done it without you, thanks",
    	    "Much appreciated", "Thanks a heap", "Thanks a lot, you're the best",
    	    "You're a lifesaver, thanks", "Forever grateful, thanks", "Thanks, you're a gem",
    	    "Thanks for being generous", "Biggest thanks to you", "Many thanks", "Thanks so much for your help",
    	    "Appreciate it greatly", "Thanks for being awesome", "You're so kind, thanks",
    	    "Can't thank you enough", "A million thanks", "You rock, thanks a lot",
    	    "Thanks for being so generous", "So kind of you, thanks", "Endless thanks",
    	    "Thanks for your generosity", "Thanks for your kindness", "You're wonderful, thanks",
    	    "Thanks for being amazing", "Truly grateful, thanks", "Much obliged, thanks",
    	    "Heartfelt thanks", "Eternally grateful, thanks", "Thanks for being so helpful",
    	    "Thanks, you're too kind", "Appreciate your kindness, thanks", "Thanks, you're awesome",
    	    "You made my day, thank you", "Thanks for everything", "Thanks for your support",
    	    "Couldn't be more thankful", "Thanks for the help", "Thanks for lending a hand",
    	    "Deeply appreciate it, thanks", "You're incredible, thank you", "Massive gratitude",
    	    "Thanks for your thoughtfulness", "Thanks a lot, seriously", "You're a legend, thank you",
    	    "Appreciate it so much, thanks", "Thanks for being a great help", "Thanks for stepping in to help",
    	    "Forever thankful", "Much respect, thanks", "Thanks for your time and help",
    	    "So thankful for you", "Thanks, you're amazing", "Thanks for the assist",
    	    "You're the best, thanks", "You�ve been so kind, thank you", "You didn�t have to, but thanks",
    	    "Thanks, you're so generous", "Thanks a million, truly", "Can't thank you enough for this",
    	    "You're a real one, thanks", "So kind of you, thank you", "You're the best, thanks again",
    	    "Thanks, this means a lot", "Thanks for looking out for me", "You're too kind, thanks",
    	    "Thanks for being thoughtful", "Thanks a lot, you're awesome", "Thanks for your kindness, truly",
    	    "Huge gratitude, thanks", "You're a blessing, thank you", "Thanks a ton, you're amazing",
    	    "Thank you from the bottom of my heart", "Forever in your debt, thanks",
    	    "Thanks so much, you're incredible", "Thanks, you really helped me out"
            });}

        static {
            conversationBank.put("farewells", new String[]{
    	    "Goodbye", "Take care", "See you later", "Thanks again, have a great day", "Bye for now",
    	    "Catch you later", "Safe travels", "Good luck with your adventures", "See you around", "Happy skilling",
    	    "Take it easy", "Farewell", "Have fun", "See you next time", "Bye, friend",
    	    "Wishing you the best", "Stay safe", "Until next time", "See you soon", "Good luck out there",
    	    "Best of luck", "Peace out", "Later", "Cheers", "See ya",
    	    "May your journey be fruitful", "Bye, adventurer", "Happy hunting", "Stay awesome", "So long",
    	    "Good luck with your grind", "Enjoy the game", "Bye, take care", "Have a good one", "Till we meet again",
    	    "See you on the flipside", "Happy adventuring", "Keep it up, see you", "Have a great adventure", "Catch you next time",
    	    "Best wishes", "See you when I see you", "Bye, and good luck", "Farewell, friend", "Bye, and stay safe",
    	    "Good luck in your quests", "Happy training", "Take care, adventurer", "Have a productive day", "Wishing you good RNG",
    	    "Take care out there", "Don't forget to have fun", "Goodbye, traveler", "May your drops be lucky", "Enjoy your grind",
    	    "Stay strong, bye", "Have a wonderful day", "See you in the wild", "Bye, and good vibes", "Hope to see you again",
    	    "Good luck with your goals", "Catch you in Gielinor", "Stay cool, bye", "Happy leveling", "See you around the GE",
    	    "Bye, have fun grinding", "Take care, and keep skilling", "Hope you reach your goals", "Until next time, friend", "Bye for now, adventurer",
    	    "May your XP be plentiful", "Stay kind, see you", "Happy exploring", "Safe travels, friend", "Have fun out there",
    	    "See you on the next quest", "Stay lucky, bye", "Goodbye for now", "See you at the bank", "Bye, and keep grinding",
    	    "Enjoy your time in-game", "Keep smashing those levels, bye", "Good luck with the loot", "Take care, and stay lucky", "See you on the trail",
    	    "Bye, and happy adventuring", "Hope you have a great session", "May the RNG gods bless you", "Catch you at the next skilling spot", "Bye, and good vibes only",
    	    "Goodbye, and have a good grind", "See you, keep gaining XP", "Happy scaping", "See you at the next drop party", "Stay awesome, and see you",
    	    "Catch you later, legend", "Stay safe, and keep skilling", "Happy questing", "Bye, and enjoy the grind", "Take care, and happy leveling"
            });}

    private static final String[] whatDoing = {
    	    "What are you up to", "What are you training right now", "Are you skilling or fighting",
    	    "How's your grind going", "What are you working on today", "What's the plan", "What task are you on",
    	    "Doing anything interesting", "Are you on a quest", "Training a skill or fighting mobs",
    	    "Where are you heading", "What are you leveling", "Are you farming or training",
    	    "How's the XP grind", "What brings you here", "What's your goal right now",
    	    "Any big plans today", "Are you hunting for drops", "Questing or just exploring",
    	    "What are you grinding today", "Are you making bank", "What skill are you focusing on",
    	    "What's the grind like today", "Are you PvPing or PvEing", "What's keeping you busy",
    	    "Any good loot so far", "Are you leveling combat", "How's the skilling going",
    	    "Training or chilling", "What level are you working on", "Are you gearing up for a fight",
    	    "Grinding XP or making GP", "What's on the agenda", "Any goals for today",
    	    "How's the adventure", "What's your focus right now", "Are you working on your skills",
    	    "Making money or XP", "What skill are you training", "What's the hustle today",
    	    "Any achievements you're chasing", "What's your target for the day",
    	    "Leveling a new skill", "Are you after rares", "What's your main skill",
    	    "Working on your next milestone", "Questing or grinding", "What's your strategy",
    	    "Are you crafting or fighting", "Any cool drops yet", "What keeps you busy here",
    	    "How's the leveling coming", "What challenge are you tackling", "Working on gear or levels",
    	    "What's your favorite skill to train", "Any new quests today", "Where's your grind taking you",
    	    "What's your current goal", "Are you focused on skilling", "How's the progress so far",
    	    "Hunting mobs or training stats", "Any good trades today", "What's your training method",
    	    "Working on magic or melee", "What keeps you motivated", "What brings you to this spot",
    	    "Any tough challenges today", "Are you farming XP", "How's the training coming along",
    	    "Leveling up or making cash", "What grind are you on", "Are you skilling for profit",
    	    "What's your next milestone", "Any big plans for your levels", "Grinding for a rare item",
    	    "What activity are you focusing on", "Are you skilling in the area", "Any progress on quests",
    	    "Working on achievements", "What's your game plan", "How's the skill grind treating you",
    	    "Any good trades happening", "What's your current mission", "What quest are you on",
    	    "Leveling up your stats", "What brings you to this zone", "Working on your setup",
    	    "Any luck with drops", "How's the grind so far", "Training for anything specific",
    	    "What skill do you like the most", "Are you mining or crafting", "What's keeping you here",
    	    "What keeps you motivated", "Are you looking for trades", "How's the adventure treating you",
    	    "What's your training priority", "What level are you aiming for", "Are you grinding for gear",
    	    "What are you building towards", "Any progress in your goals", "How's your skilling adventure"
    	};

    private static final String[] askAboutPlayer = {
    	    "What's your combat level", "How long have you been playing", "What skill are you training",
    	    "What's your favorite skill", "Are you a PvMer or skiller", "Do you enjoy quests",
    	    "What's your total level", "Do you have any advice for a new player", "What's your highest skill",
    	    "Do you play often", "How did you get started", "What's your most prized item",
    	    "Do you prefer PvP or PvE", "What's your go-to skill to train", "Do you grind a lot",
    	    "What's your favorite boss to fight", "What's the best drop you've ever gotten",
    	    "Do you play on mobile or PC", "What's your best money-making method",
    	    "Do you like the community here", "What's your rarest item", "How did you get your gear",
    	    "What's your clan like", "Do you train one skill at a time", "What's your next goal",
    	    "Do you quest often", "How high is your Slayer level", "Do you enjoy bossing",
    	    "What's your favorite place to train", "How do you make most of your GP",
    	    "Are you more of a casual player", "Do you prefer skilling or combat",
    	    "What's the hardest quest you've done", "Do you have a favorite pet",
    	    "What's your longest grind", "Have you maxed any skills", "What world do you usually play on",
    	    "What's your favorite thing about this game", "Do you trade often", "What's your favorite weapon",
    	    "Have you completed all quests", "What's your least favorite skill to train",
    	    "Do you play other games", "What motivates you to play", "Do you use the Grand Exchange often",
    	    "What's the best advice you've gotten", "How do you level up so fast",
    	    "Do you have a favorite outfit", "What's your favorite training spot",
    	    "Do you usually play solo", "What's your proudest in-game moment",
    	    "How do you handle tough bosses", "Do you prefer PvM or skilling",
    	    "What's your main goal right now", "How do you choose your tasks",
    	    "Have you ever gotten a super rare drop", "Do you participate in minigames",
    	    "What's your go-to skill for making GP", "Have you maxed any combat stats",
    	    "What's the most fun activity in-game", "Do you enjoy fishing or woodcutting more",
    	    "What's the best armor you've owned", "Do you use alts to play",
    	    "How do you balance skilling and combat", "Do you like the seasonal events",
    	    "Have you joined a clan yet", "What's your favorite holiday event",
    	    "Do you prefer long grinds or quick tasks", "What's your favorite achievement so far",
    	    "Have you unlocked all teleports", "What's your favorite training strategy",
    	    "How did you get into this game", "Do you farm resources or buy them",
    	    "What's your fastest skill to level", "Do you like questing for lore",
    	    "How often do you play each week", "What's your least favorite monster to fight",
    	    "Do you use any special tactics for leveling", "What keeps you coming back to this game",
    	    "Have you ever completed a skill cape grind", "What's your favorite thing to do with friends in-game",
    	    "Do you prefer melee, magic, or ranged", "What's the best quest reward you've gotten",
    	    "Have you unlocked your dream item yet", "How do you approach big goals",
    	    "What's your strategy for earning GP", "Do you like exploring the map",
    	    "What's the rarest thing you've ever owned", "Have you done group content",
    	    "What's your advice for someone starting out", "How do you handle downtime in-game",
    	    "Do you enjoy crafting or gathering more", "What's your favorite way to relax in-game",
    	    "Do you use any unique builds", "How do you train efficiently",
    	    "Do you go for max efficiency or casual play", "What's your biggest achievement in this game",
    	    "Have you ever hosted events", "What's your favorite skilling outfit",
    	    "Do you have a favorite NPC", "What's your most memorable in-game moment",
    	    "How do you handle competitive areas", "What's your go-to grind when bored",
    	    "Do you prefer solo or group activities"
    	};

    private static final String[] responsesToGenerosity = {
    	    "You're the best, thank you", "That helps so much, thanks", "You just made my day",
    	    "Really appreciate your kindness", "You're amazing, thanks a ton", "This means so much, thank you",
    	    "Wow, you're so generous", "Thanks, I'll never forget this", "You're a lifesaver", "Big thanks for helping me",
    	    "This means the world to me, thank you", "You're too kind, I can't thank you enough", "You have no idea how much this helps",
    	    "I owe you big time, thank you", "Your generosity is incredible", "You're a legend, thanks so much",
    	    "This will go a long way, thank you", "You're so kind-hearted, thanks a ton", "You're my hero, thank you",
    	    "I can't believe it, thank you so much", "You're truly amazing, thanks", "Such a kind gesture, thank you",
    	    "You're making such a big difference for me", "This is such a blessing, thank you", "Wow, just wow, thank you",
    	    "You're a gift to the community, thank you", "You're so thoughtful, thanks", "This is the help I really needed, thank you",
    	    "I can't say thank you enough", "You're making my goals possible, thank you", "I appreciate you so much",
    	    "You're too generous, thank you", "Thanks for being awesome", "You're an absolute star, thanks",
    	    "This is beyond kind of you, thank you", "You're the real MVP, thanks so much", "Can't thank you enough for this",
    	    "Your kindness is inspiring, thanks", "This makes everything better, thank you", "You're the reason I'm smiling, thanks",
    	    "Thanks for believing in me", "You didn't have to, but I'm so grateful", "You're a saint, thank you",
    	    "Thanks for looking out for me", "Such generosity is rare, thank you", "You've made my journey so much easier",
    	    "Thanks for being so thoughtful", "You�re truly one of a kind, thanks", "You're the highlight of my day, thank you",
    	    "Such an amazing gift, thank you", "This is going to help so much, thanks", "Thanks for being so giving",
    	    "You're an example of kindness, thanks", "This makes a huge difference, thank you", "You're the reason I keep playing, thanks",
    	    "Thanks for your trust and support", "You're making my dream a reality, thanks", "You just boosted my morale, thank you",
    	    "This changes everything for me, thanks", "You�re incredibly generous, thank you", "Thanks for your selflessness",
    	    "You really came through for me, thanks", "Your kindness doesn't go unnoticed, thank you", "This is more than I could ask for, thanks",
    	    "Thanks for your encouragement", "This gesture means so much, thank you", "You're the kindest player I've met, thanks",
    	    "Thanks for being there for me", "You�ve really helped me out, thanks", "You�re setting an amazing example, thank you",
    	    "Thanks for your generosity, it inspires me", "You're lifting me up, thanks", "This is such a boost, thank you",
    	    "Thanks for your time and kindness", "You're spreading good vibes, thanks", "Thanks for being part of my journey",
    	    "You make this game better, thank you", "This act of kindness won�t be forgotten, thanks", "You�re a blessing, thank you",
    	    "Thanks for believing in a newbie like me", "Your generosity fuels my grind, thanks", "This is a game-changer, thank you",
    	    "Thanks for making my day brighter", "You�ve restored my faith in the community, thanks", "You're so thoughtful, thank you",
    	    "Thanks for being such a big help", "Your kindness shines through, thank you", "Thanks for giving me a chance",
    	    "You just gave me hope, thank you", "This is beyond my expectations, thanks", "You�re making such a difference, thank you",
    	    "Thanks for being so giving", "You�ve really lifted me up, thank you", "This generosity is inspiring, thanks",
    	    "You�ve set a great example, thank you", "This support means the world to me, thanks", "You're so awesome, thank you"
    	};

    private static final String[] whyBegging = {
    	    "Just trying to progress faster", "It's hard to get started, so every gp helps", "I'm saving for better gear",
    	    "New player struggles, you know how it is", "Trying to level up my skills", "Everyone needs a little help sometimes",
    	    "Saving up for a big goal, so I thought I'd ask", "I like seeing how kind people can be", "Skilling is slow without funds",
    	    "I'm testing how helpful the community is", "Training combat takes resources", "Saving up for some runes",
    	    "Trying to buy better armor", "Need money to train crafting", "Saving for supplies to level my cooking",
    	    "Hoping to buy my first good weapon", "Collecting gp for some quests", "It's tough being new here",
    	    "Saving for membership", "Need gp to afford skilling materials", "Trying to fund my magic training",
    	    "Saving for dragon gear", "Starting fresh, so I need a boost", "Looking for help to build my bank",
    	    "Trying to keep up with other players", "It's just so expensive to train certain skills", "Trying to get better tools for mining",
    	    "Need gp for teleport tabs", "Saving for my dream cape", "Want to afford better fishing gear",
    	    "Looking to buy some herbs to start herblore", "I want to train construction but it's costly", "Saving up for cannonballs",
    	    "Need gp to level my range skill", "Trying to afford prayer potions", "Saving for a good amulet",
    	    "Training is slower without better gear", "Trying to save for barrows gloves", "Working toward 99 woodcutting but need help",
    	    "It's hard to grind without a little support", "Trying to rebuild after losing my stuff", "Need gp for dragon bones",
    	    "Hoping to get better supplies for skilling", "Saving for a rune pickaxe", "Want to afford a full set of rune armor",
    	    "Trying to buy a glory amulet", "Need money to train agility", "Saving for sharks to train combat",
    	    "Looking to buy a new shield", "Training farming but seeds are expensive", "Need gp to buy pure essence",
    	    "Trying to buy some better boots", "Saving for a new cape", "Want to train summoning but it's costly",
    	    "Trying to get into PvM but need better gear", "Saving for a skill cape", "Hoping to fund my runecrafting training",
    	    "Need money to buy feathers for fishing", "Trying to train smithing but bars are expensive", "Want to start flipping in the GE",
    	    "Saving for a new ring", "Need gp for training hunter", "Trying to get better gear for slayer",
    	    "Saving for my first whip", "Hoping to buy some prayer scrolls", "Looking for help to train divination",
    	    "Need gp to buy seeds for farming", "Trying to afford teleport jewelry", "Saving for a dragonfire shield",
    	    "Want to buy a rune crossbow", "Training construction but planks are costly", "Trying to fund my fletching training",
    	    "Hoping to afford a decent weapon", "Looking to buy a better magic staff", "Saving for a good set of boots",
    	    "Need gp to buy arrows for range training", "Trying to level crafting but it's expensive", "Looking to start doing boss fights",
    	    "Saving for some decent food to train combat", "Hoping to afford a dragon scimitar", "Need money for potions",
    	    "Trying to buy my first godsword", "Saving for some skilling outfits", "Looking for gp to fund my slayer tasks",
    	    "Training construction and need gp for planks", "Trying to afford an upgrade to my bank space", "Saving for runes to train magic",
    	    "Hoping to get better gloves for combat", "Need gp for firemaking supplies", "Trying to buy my first skill cape",
    	    "Looking for help to train my summoning skill", "Saving for a barrows set", "Hoping to afford a rune defender",
    	    "Need gp for ranged ammo", "Trying to save up for a quest item", "Looking for a boost to buy dragon gear",
    	    "Saving for supplies to train invention", "Hoping to get a teleport tab set", "Training fishing and need bait",
    	    "Trying to buy some logs to train firemaking"
    	};

    private static final String[] botQuestions = {
    	    "Of course not, why would you think that", "Nope, just trying to enjoy the game", "I'm definitely not a bot, lol",
    	    "No way, just a regular player here", "I'm as real as they come", "Not a bot, just chatting and playing",
    	    "Why would you think I'm a bot", "I�m just a noob trying to play", "Definitely not a bot, haha",
    	    "Bots don�t talk this much, do they", "Nope, just enjoying the grind", "Do I really seem like a bot",
    	    "Not a chance, I�m just here to have fun", "Nah, bots don�t reply like this", "I�m not, but thanks for asking",
    	    "What makes you think that", "Lol no, I�m just broke", "Bots don�t need GP, right", "I�m just trying to get started",
    	    "Nope, real player here", "Why does everyone think I�m a bot", "I�d be a terrible bot if I was one",
    	    "Nope, I�m too bad at this game to be a bot", "Nah, I�m just a newbie", "Bots don�t beg for GP, right",
    	    "Lol no, bots don�t have this much personality", "I�m just trying to learn the game", "Nope, I�m here to make friends too",
    	    "Nah, I�m just really social", "Bots can�t hold conversations, can they", "Do bots even ask for advice",
    	    "Haha, no, I�m just trying to get better", "I�m too slow to be a bot", "I wish I was as efficient as a bot",
    	    "Nope, I just type fast", "No way, bots don�t make jokes", "You�re talking to a real player here",
    	    "Not a bot, just a little broke", "I�m just here for the fun", "Why would I be a bot", "Do I seem that robotic",
    	    "Not a bot, just struggling like everyone else", "I�m too chatty to be a bot", "Bots don�t need friends, right",
    	    "I�m here to enjoy the community", "Not a bot, just trying to get by", "Haha, nope, just a regular player",
    	    "Do bots even have personalities", "Nah, I�m just really bad at making money", "I�m a human, I promise",
    	    "Why would I be a bot, lol", "I�m just trying to get some help", "No way, bots don�t ask how you�re doing",
    	    "Definitely not, bots can�t do this", "Bots can�t talk about quests, right", "I�m real, but thanks for asking",
    	    "Lol no, just trying to have a good time", "Do bots beg this much", "Nah, I�m just bad at the game",
    	    "Not a bot, just struggling to level up", "Why does everyone think I�m a bot", "Nope, real player vibes only",
    	    "I�m too unpredictable to be a bot", "Not a bot, just trying to survive", "I wouldn�t know how to be a bot",
    	    "Bots don�t say thank you, right", "I�m real, bots are boring", "You�re talking to a real player, I swear",
    	    "Bots can�t hold conversations like this", "Nope, I�m just broke and friendly", "I�m just a player looking for GP",
    	    "Haha, no, I�m just bad at making money", "I�m here to enjoy the game like you", "Bots don�t have feelings, right",
    	    "Do I seem that efficient to you", "I promise I�m not a bot", "I�m just here to chill and play",
    	    "Why does everyone think I�m automated", "Not a bot, just a noob", "I�m too talkative to be a bot",
    	    "Bots don�t ask for advice, do they", "I�m just broke, not a bot", "Nope, I�m just terrible at making GP",
    	    "I�m just here for the social side too", "Do I seem like a robot to you", "Bots can�t enjoy the grind like this",
    	    "Haha, no, bots can�t laugh", "Definitely not, I�m just slow", "I�m real, bots are too boring for me",
    	    "Bots don�t struggle like I do", "No way, I�m here to have fun", "I�m not a bot, but I appreciate the compliment",
    	    "I�m human, bots are way more efficient", "Haha, no, I�m too bad at this game to be a bot"
    	};

    private static final String[] howMuchGpResponses = {
    	    "10k would be great", "Just 15k", "20k would help", "25k would make a difference", "30k for gear", "50k if you can spare it",
    	    "40k should do it", "Could use 100k", "150k would be amazing", "Just 200k for now", "300k to save up",
    	    "How about 75k?", "125k would help a lot", "50k is all I need", "20k to start", "15k for some supplies",
    	    "10k for basic items", "250k would set me up", "30k for skilling", "50k to progress faster",
    	    "100k for upgrades", "20k for tools", "150k to buy better gear", "200k would change everything",
    	    "Just 10k to get started", "75k for training", "300k would be incredible", "40k for my goals", "25k for essentials",
    	    "50k for my grind", "100k for my plans", "150k to get ahead", "30k for supplies", "250k would mean the world",
    	    "200k to reach my target", "10k is enough for now", "20k would be great", "50k for armor upgrades",
    	    "100k for crafting", "150k would really help", "300k for the dream gear", "75k for better equipment",
    	    "125k to keep going", "40k to get started", "25k to help out", "50k to level up", "200k for big goals",
    	    "250k for supplies", "15k would be fine", "30k to buy training gear", "75k to get ahead", "100k for tools",
    	    "150k to make progress", "300k for long-term plans", "50k to buy gear", "20k for quick supplies", "10k to kick off",
    	    "250k to invest in skills", "200k to help me advance", "125k for better weapons", "75k for upgrades",
    	    "30k for skilling items", "15k to get started", "40k to cover basics", "100k for my setup", "50k to move forward",
    	    "150k to complete my build", "300k to achieve my goals", "200k for progress", "250k to hit my milestones",
    	    "20k for beginner tools", "10k for starting out", "25k for quick upgrades", "50k for supplies",
    	    "100k to push forward", "125k for gear improvements", "75k to boost my grind", "150k to change everything",
    	    "250k to set up properly", "300k to make real progress", "200k for long-term plans", "15k for small upgrades",
    	    "30k for basic needs", "10k to keep going", "40k for my skilling setup", "20k to level faster",
    	    "150k to fund my goals", "100k for short-term plans", "300k to finish my dream setup", "75k for new items",
    	    "250k to buy what I need", "50k for upgrades", "200k for leveling supplies", "125k to complete my setup",
    	    "30k to cover training", "25k for crafting gear", "10k for the essentials", "300k to really help me out",
    	    "40k for minor upgrades", "20k to move ahead", "50k to keep me grinding", "150k for the next step"
    	};


    /**
     * Processes incoming chat messages, responds based on regex patterns,
     * and notifies TradeHandler of trade invitations.
     *
     * @param message       The incoming chat message.
     * @param respondToChat A Consumer to handle chat responses.
     * @param tradeHandler  The TradeHandler instance to notify of trade invitations.
     * @param script        The main script instance for accessing player information.
     */
    public static void handleChatMessage(Message message, Consumer<String> respondToChat, TradeHandler tradeHandler, Script script) {
        try {
            String chatMessage = message.getMessage().toLowerCase();
            String sender = message.getUsername();

            // Fetch the bot's player name dynamically
            String botPlayerName = script.myPlayer().getName();

            // Ignore messages from the bot itself
            if (sender == null || sender.equalsIgnoreCase(botPlayerName)) {
                return;
            }

            // Check for trade invitation messages
            if (chatMessage.contains("wishes to trade with you") || chatMessage.contains("trades with you")) {
                tradeHandler.onTradeInvitation(sender);
                logTradeInvitation(sender);
                return;
            }

            // Check cooldown for responses
            if (recentResponses.containsKey(sender) && System.currentTimeMillis() - recentResponses.get(sender) < RESPONSE_COOLDOWN) {
                return;
            }

            // Match chat messages against regex patterns
            for (Map.Entry<String, String[]> entry : regexResponses.entrySet()) {
                if (chatMessage.matches(entry.getKey())) {
                    String response = getRandomResponse(entry.getValue());
                    respondToChat.accept(response);
                    logResponse(sender, chatMessage, response);

                    // Update cooldown for the user
                    recentResponses.put(sender, System.currentTimeMillis());
                    return;
                }
            }

            // Fallback: If no regex matches, greet the user
            respondToChat.accept(getRandomResponse(greetings));
        } catch (Exception e) {
            script.log("Error in Conversations.handleChatMessage: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets a random response from an array of responses.
     *
     * @param responses The array of possible responses.
     * @return A randomly selected response.
     */
    private static String getRandomResponse(String[] responses) {
        return responses[new Random().nextInt(responses.length)];
    }

    /**
     * Logs the trade invitation detection.
     *
     * @param sender The name of the player who sent the trade invitation.
     */
    private static void logTradeInvitation(String sender) {
        System.out.println("[Conversations] Trade invitation detected from: " + sender);
    }

    /**
     * Logs the matched chat response.
     *
     * @param sender       The sender of the message.
     * @param chatMessage  The incoming chat message.
     * @param response     The response being sent.
     */
    private static void logResponse(String sender, String chatMessage, String response) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        System.out.printf("[%s] [Conversations] From: %s, Matched: \"%s\", Responded: \"%s\"%n", timestamp, sender, chatMessage, response);
    }
}

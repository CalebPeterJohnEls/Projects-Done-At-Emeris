namespace FunInAction.Mvc.Services;

public static class ContractTerms
{
    public static List<(string Heading, string Text)> For(string? title)
    {
        var t = (title ?? "").ToLowerInvariant();

        if (t.Contains("photo") || t.Contains("media") || t.Contains("image")) return Photo;
        if (t.Contains("medical") || t.Contains("health") || t.Contains("injury")) return Medical;
        if (t.Contains("conduct") || t.Contains("behav") || t.Contains("rules")) return Conduct;
        if (t.Contains("payment") || t.Contains("fee")) return Payment;
        return General;
    }

    private static readonly List<(string, string)> General = new()
    {
        ("Participation", "The player takes part in Fun in Action sessions voluntarily and will follow the coach's instructions during every session."),
        ("Risk", "Sports activities carry a risk of injury. By signing, you accept this risk on behalf of the player and confirm the player is fit to take part."),
        ("Supervision", "Coaches supervise sessions only. Parents and guardians remain responsible for the player before and after each session."),
        ("Changes and cancellations", "Sessions may be moved or cancelled because of weather, safety or low attendance. Fun in Action will tell you as early as possible."),
        ("Your agreement", "By ticking the box you confirm that you have read and understood this agreement and that you are the parent, guardian or player entitled to sign it.")
    };

    private static readonly List<(string, string)> Photo = new()
    {
        ("What we may capture", "Photos and videos of the player during sessions, events and matches."),
        ("How we may use them", "For Fun in Action's website, social media, newsletters and promotional material. We will not use them for anything unrelated to the programme."),
        ("Personal information", "We handle images and any names shown with them in line with the Protection of Personal Information Act (POPIA)."),
        ("Withdrawing consent", "You can withdraw this consent at any time by telling the coach in writing. We will stop using new images straight away and remove existing ones where reasonably possible."),
        ("Your agreement", "By ticking the box you confirm that you have read this consent and agree to it.")
    };

    private static readonly List<(string, string)> Medical = new()
    {
        ("Medical information", "You confirm that any medical conditions, allergies or medication the coach should know about have been given to us and are up to date."),
        ("Emergency treatment", "If the player is injured or becomes ill, you allow the coach to arrange first aid and to contact emergency services when needed."),
        ("Contact", "We will try to reach you or your emergency contact as soon as possible. Please keep your phone number current."),
        ("Privacy", "Medical information is kept private, shared only with the people who need it to keep the player safe, and handled in line with POPIA."),
        ("Your agreement", "By ticking the box you confirm that you have read this form and that the information you have given is correct.")
    };

    private static readonly List<(string, string)> Conduct = new()
    {
        ("Respect", "Players, parents and guardians treat coaches, other players and facilities with respect at all times."),
        ("Safety", "Players follow safety instructions and do not use equipment without permission."),
        ("Behaviour", "Bullying, abusive language or dangerous play is not allowed. Repeated breaches may lead to a player being suspended from sessions."),
        ("Parents and guardians", "Please support all players, and leave coaching decisions to the coaches."),
        ("Your agreement", "By ticking the box you confirm that you have read this code and will follow it.")
    };

    private static readonly List<(string, string)> Payment = new()
    {
        ("Fees", "You agree to pay the programme fees shown on the Payments page by their due dates."),
        ("Payment methods", "Payments can be made by card or by EFT with proof of payment. A payment counts as received once the coach confirms it."),
        ("Missed sessions", "Fees are not reduced for sessions a player chooses to miss, unless the coach agrees otherwise in writing."),
        ("Late payment", "If a payment is overdue, the coach may pause the player's place until the account is up to date."),
        ("Your agreement", "By ticking the box you confirm that you have read these payment terms and agree to them.")
    };
}
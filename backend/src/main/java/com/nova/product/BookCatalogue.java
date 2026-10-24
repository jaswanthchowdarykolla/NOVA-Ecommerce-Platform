package com.nova.product;

import java.util.List;

/**
 * Representative NOVA book catalogue. Titles and authors are real published works.
 * Cover images are Unsplash photography used as catalogue art, not official publisher covers.
 * ISBNs and editions are intentionally omitted so they are not presented as verified facts.
 */
public final class BookCatalogue {
    public record Entry(
            long preferredId,
            String slug,
            String title,
            String author,
            String category,
            String genre,
            int priceInr,
            String unsplashId,
            String description,
            boolean featured
    ) { }

    private BookCatalogue() { }

    public static List<Entry> entries() {
        return List.of(
                novel(1001, "pride-and-prejudice", "Pride and Prejudice", "Jane Austen", "Classics / Romance", 499, "photo-1544947950-fa07a98d237f",
                        "Austen's comedy of manners follows Elizabeth Bennet as wit, pride and social expectation collide in Georgian England.", true),
                novel(1002, "frankenstein", "Frankenstein", "Mary Shelley", "Classics / Science fiction", 449, "photo-1512820790803-83ca734da794",
                        "Shelley's foundational gothic novel of creation, responsibility and the limits of scientific ambition.", true),
                novel(1003, "the-great-gatsby", "The Great Gatsby", "F. Scott Fitzgerald", "Literary fiction", 479, "photo-1541963463532-d68292c34b19",
                        "A spare portrait of longing and illusion on Long Island in the Jazz Age.", true),
                novel(1004, "to-kill-a-mockingbird", "To Kill a Mockingbird", "Harper Lee", "Literary fiction", 529, "photo-1495446815901-a7297e633e8d",
                        "Scout Finch's childhood in Maycomb becomes a study of justice, courage and moral education.", false),
                novel(1005, "nineteen-eighty-four", "Nineteen Eighty-Four", "George Orwell", "Science fiction", 499, "photo-1481627834876-b7833e8f5570",
                        "Orwell's warning of surveillance, language and political control in a totalitarian future.", false),
                novel(1006, "the-hobbit", "The Hobbit", "J.R.R. Tolkien", "Fantasy", 599, "photo-1519681393784-d120267933ba",
                        "Bilbo Baggins leaves the Shire for a quest of dragons, riddles and unexpected courage.", false),
                novel(1007, "murder-on-the-orient-express", "Murder on the Orient Express", "Agatha Christie", "Mystery and thriller", 549, "photo-1476275466078-4007374efbbe",
                        "Hercule Poirot investigates a murder aboard a snowbound luxury train.", false),
                novel(1008, "the-hound-of-the-baskervilles", "The Hound of the Baskervilles", "Arthur Conan Doyle", "Mystery and thriller", 429, "photo-1457369804613-52c61a468e7d",
                        "Holmes and Watson confront a family curse and a spectral hound on Dartmoor.", false),
                novel(1009, "jane-eyre", "Jane Eyre", "Charlotte Brontë", "Classics / Romance", 479, "photo-1516979187457-637abb4f9353",
                        "An orphaned governess claims independence, love and moral clarity at Thornfield Hall.", false),
                novel(1010, "wuthering-heights", "Wuthering Heights", "Emily Brontë", "Classics / Literary fiction", 459, "photo-1507842217343-583bb7270b66",
                        "A storm of passion and revenge on the Yorkshire moors, told through interlocking narrators.", false),
                novel(1011, "the-picture-of-dorian-gray", "The Picture of Dorian Gray", "Oscar Wilde", "Classics / Literary fiction", 449, "photo-1524995997946-a1c2e315a42f",
                        "A beautiful young man bargains with time while a portrait records the cost of his desires.", false),
                novel(1012, "dune", "Dune", "Frank Herbert", "Fantasy and science fiction", 799, "photo-1446776811953-b23d57bd21aa",
                        "Politics, ecology and prophecy collide on the desert planet Arrakis.", false),
                novel(1013, "rebecca", "Rebecca", "Daphne du Maurier", "Mystery and thriller", 529, "photo-1521587760476-6c12a4b040da",
                        "A nameless narrator enters Manderley and the long shadow of her husband's first wife.", false),
                novel(1014, "the-left-hand-of-darkness", "The Left Hand of Darkness", "Ursula K. Le Guin", "Fantasy and science fiction", 649, "photo-1463320726281-696a485898c8",
                        "An envoy to the ice world of Gethen discovers a society whose ideas of gender unsettle his own.", false),
                novel(1015, "beloved", "Beloved", "Toni Morrison", "Literary fiction / Historical fiction", 599, "photo-1497633762265-9d179a990aa6",
                        "A mother in Reconstruction-era Ohio is visited by the child she refused to surrender to slavery.", false),
                novel(1016, "the-count-of-monte-cristo", "The Count of Monte Cristo", "Alexandre Dumas", "Classics / Historical fiction", 699, "photo-1532012197267-da84d127e765",
                        "Wrongfully imprisoned Edmond Dantès returns as an instrument of elaborate justice.", false),
                novel(1017, "circe", "Circe", "Madeline Miller", "Fantasy / Literary fiction", 699, "photo-1519682337058-a94d519337bc",
                        "The witch of Aiaia tells her own story of exile, craft and reluctant power among gods and mortals.", false),
                novel(1018, "pachinko", "Pachinko", "Min Jin Lee", "Historical fiction", 749, "photo-1529156069898-49953e39b3ac",
                        "Four generations of a Korean family navigate love, work and belonging in twentieth-century Japan.", false),
                novel(1019, "the-midnight-library", "The Midnight Library", "Matt Haig", "Fiction", 599, "photo-1512820790803-83ca734da794",
                        "Between life and death, a library of unlived lives asks what it means to choose differently.", false),
                novel(1020, "the-silent-patient", "The Silent Patient", "Alex Michaelides", "Mystery and thriller", 649, "photo-1455885333736-6d7f34ca9d4b",
                        "A psychotherapist becomes obsessed with an artist who has not spoken since she was accused of murder.", false),
                novel(1021, "normal-people", "Normal People", "Sally Rooney", "Literary fiction / Romance", 549, "photo-1519681393784-d120267933ba",
                        "Connell and Marianne move from a small Irish town to Dublin, testing intimacy against class and silence.", false),
                novel(1022, "the-night-circus", "The Night Circus", "Erin Morgenstern", "Fantasy", 649, "photo-1507842217343-583bb7270b66",
                        "Two young magicians are bound to a nocturnal circus that is itself the arena of their contest.", false),
                textbook(1023, "introduction-to-algorithms", "Introduction to Algorithms", "Thomas H. Cormen, Charles E. Leiserson, Ronald L. Rivest, Clifford Stein", "Computer Science", 2499, "photo-1515879218367-8466d910aaa4",
                        "The standard undergraduate and graduate reference for algorithm design, analysis and proofs, used across computer-science programmes.", true),
                textbook(1024, "computer-networks", "Computer Networks", "Andrew S. Tanenbaum, David J. Wetherall", "Computer Science", 1899, "photo-1558494949-ef010cbdcc31",
                        "A layered introduction to network architecture, protocols and the practical design of the internet.", true),
                textbook(1025, "operating-system-concepts", "Operating System Concepts", "Abraham Silberschatz, Peter B. Galvin, Greg Gagne", "Computer Science", 1999, "photo-1518770660439-4636190af475",
                        "Process management, memory, file systems and concurrency as taught in core operating-systems courses.", false),
                textbook(1026, "clean-code", "Clean Code", "Robert C. Martin", "Programming and software engineering", 1299, "photo-1461749280684-dccba630e2f6",
                        "A practitioner's guide to readable, testable software and the habits that keep large codebases maintainable.", false),
                textbook(1027, "designing-data-intensive-applications", "Designing Data-Intensive Applications", "Martin Kleppmann", "Programming and software engineering", 1899, "photo-1555949963-aa79dcee981c",
                        "Reliability, scalability and maintainability in distributed data systems, from storage engines to stream processing.", false),
                textbook(1028, "the-pragmatic-programmer", "The Pragmatic Programmer", "Andrew Hunt, David Thomas", "Programming and software engineering", 1199, "photo-1515879218367-8466d910aaa4",
                        "Career-long craft advice for software engineers: tooling, design, communication and continuous learning.", false),
                textbook(1029, "pattern-recognition-and-machine-learning", "Pattern Recognition and Machine Learning", "Christopher M. Bishop", "Data Science and Machine Learning", 2299, "photo-1509228468518-180dd4864904",
                        "A mathematically grounded graduate text on Bayesian methods, graphical models and statistical learning.", false),
                textbook(1030, "hands-on-machine-learning", "Hands-On Machine Learning with Scikit-Learn, Keras, and TensorFlow", "Aurélien Géron", "Data Science and Machine Learning", 1799, "photo-1555949963-ff9fe0c870eb",
                        "A project-led path through classical ML and deep learning with widely used Python libraries.", false),
                textbook(1031, "python-for-data-analysis", "Python for Data Analysis", "Wes McKinney", "Data Science and Machine Learning", 1499, "photo-1526374965328-7f61d4dc18c5",
                        "Pandas, NumPy and the practical workflow for cleaning, shaping and analysing tabular data.", false),
                textbook(1032, "calculus-stewart", "Calculus", "James Stewart", "Mathematics and Statistics", 2199, "photo-1635070041078-e363dbe005cb",
                        "A widely adopted calculus text covering limits, derivatives, integrals and infinite series.", false),
                textbook(1033, "linear-algebra-and-its-applications", "Linear Algebra and Its Applications", "David C. Lay, Steven R. Lay, Judi J. McDonald", "Mathematics and Statistics", 1699, "photo-1509228468518-180dd4864904",
                        "Matrix algebra, vector spaces and linear transformations with applications used in STEM degrees.", false),
                textbook(1034, "introduction-to-probability", "Introduction to Probability", "Joseph K. Blitzstein, Jessica Hwang", "Mathematics and Statistics", 1599, "photo-1635070041078-e363dbe005cb",
                        "A story-driven probability course emphasising counting, conditioning and random variables.", false),
                textbook(1035, "university-physics", "University Physics", "Hugh D. Young, Roger A. Freedman", "Physics", 2399, "photo-1636466497217-0a78d1e6d3b2",
                        "Calculus-based mechanics, waves, thermodynamics, electricity and magnetism for first-year physics.", false),
                textbook(1036, "conceptual-physics", "Conceptual Physics", "Paul G. Hewitt", "Physics", 1399, "photo-1446776811953-b23d57bd21aa",
                        "An accessible treatment of physical principles emphasising intuition before formal calculation.", false),
                textbook(1037, "principles-of-economics", "Principles of Economics", "N. Gregory Mankiw", "Business and Economics", 1899, "photo-1554224155-6726b3ff858f",
                        "Microeconomic and macroeconomic foundations used in introductory university economics courses.", false),
                textbook(1038, "corporate-finance", "Corporate Finance", "Stephen A. Ross, Randolph W. Westerfield, Jeffrey Jaffe", "Business and Economics", 2099, "photo-1579621970563-ebec7560ff3e",
                        "Valuation, capital structure and investment decisions as taught in business and finance programmes.", false),
                textbook(1039, "artificial-intelligence-a-modern-approach", "Artificial Intelligence: A Modern Approach", "Stuart Russell, Peter Norvig", "Computer Science", 2599, "photo-1677442136019-21780ecad995",
                        "The standard survey of search, knowledge representation, learning and intelligent agents.", false),
                textbook(1040, "database-system-concepts", "Database System Concepts", "Abraham Silberschatz, Henry F. Korth, S. Sudarshan", "Computer Science", 1999, "photo-1544383835-bda2bc66aa55",
                        "Relational design, SQL, transactions and storage internals for database courses.", false)
        );
    }

    private static Entry novel(long id, String slug, String title, String author, String genre, int price, String image, String description, boolean featured) {
        return new Entry(id, "novel-" + slug, title, author, "Novels", genre, price, image, description, featured);
    }

    private static Entry textbook(long id, String slug, String title, String author, String genre, int price, String image, String description, boolean featured) {
        return new Entry(id, "textbook-" + slug, title, author, "Textbooks", genre, price, image, description, featured);
    }
}

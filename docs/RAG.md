## RAG Assistant

The platform integrates a Retrieval-Augmented Generation assistant that allows users to ask natural language questions about NFL data and receive accurate, grounded answers. Rather than relying on a language model's general training knowledge, the assistant retrieves relevant information from the application's own database before generating a response. This approach ensures that answers are based on the actual data imported into the platform rather than on generic or potentially outdated sports knowledge.

The system uses two external APIs in combination. VoyageAI is used to convert text into dense vector embeddings, and Anthropic Claude is used to generate the final natural language response. Both the document indexing step and the query step go through these services, and both require API keys configured via environment variables before the assistant can function.

### How It Works

When a user submits a question through the assistant endpoint, the system follows three steps. First, the question is embedded into a vector using VoyageAI's voyage-3-lite model with the input type set to query. Second, a similarity search is executed against the document_chunks table in PostgreSQL using the pgvector extension, which returns the five most semantically relevant text chunks stored in the database. Third, those chunks are assembled into a context block and passed to Claude alongside the original question and a system prompt that instructs the model to act as a professional NFL analyst and respond only from the provided context, without fabricating statistics or speculating beyond what the data supports.

The embedding column in the document_chunks table uses pgvector's native vector type and is not mapped through JPA. Instead, the embedding is written directly using a JDBC update after the chunk record is saved, which is the standard workaround for storing pgvector values through Spring Data.

### Indexed Document Types

The quality of the assistant's answers depends entirely on what has been indexed. The platform currently supports three types of source documents, each targeting a different level of granularity.

Game reports are the first source type. Each generated game report is stored as a document chunk with its embedding, allowing the assistant to answer questions about specific matches, performances in a given game, or notable plays. These are indexed through the game report ingestion pipeline and are automatically skipped on subsequent runs if already embedded.

Player season summaries are the second source type. Each summary is a single coherent document that aggregates a player's full season statistics across all categories, their injury report history with confirmed missed games derived from the absence of matching PlayerStats records, weekly snap count usage with flagged drops in playing time, and their active contract details including total value, average per year, and cap percentage. Combining all of this into one document means the assistant can answer cross-table questions such as whether a player's reduced output was caused by injury, by a reduced role as shown by snap counts, or by both simultaneously.

Team season summaries are the third source type. Each summary covers the team's season record and head coach, average offensive yards and turnovers per game, the full list of draft picks in that year with round, pick number, and position, all trades in which the team either sent or received assets, and the total number of injury report entries for the season. This gives the assistant enough context to answer questions about front office decisions, roster construction, and how a team's performance correlated with its personnel moves.

### Ingestion Endpoints

All indexing operations are protected by the ADMIN role and are triggered manually via POST requests. The game report pipeline at POST /api/admin/rag/index processes all stored reports not yet embedded. The player summary pipeline at POST /api/admin/rag/index/player-summaries/{season} generates and indexes one document per player who appeared in any game of the given season. The team summary pipeline at POST /api/admin/rag/index/team-summaries/{season} generates one document per team. All three pipelines are idempotent and include a 200 millisecond delay between embedding calls to stay within VoyageAI's rate limits.

### Usage

The assistant is available to all users through the endpoint POST /api/assistant/ask, which accepts a JSON body with a question field. The response includes both the original question and the generated answer. If no relevant chunks are found in the database, the assistant returns a fixed message indicating insufficient indexed data rather than attempting to answer from general knowledge.

The recommended order of operations before using the assistant for the first time is to run the data import pipelines for the desired seasons, generate game reports for the completed games, and then trigger the three ingestion endpoints in sequence. Player and team summary indexing should be re-run after importing new seasons or after game reports are regenerated, since the embeddings are not updated automatically.

### What Still Needs to Be Implemented

The current implementation covers the core RAG pipeline but several improvements are planned. Filtering the similarity search by season, team, or source type before running the vector comparison would improve precision for narrow questions and reduce noise from unrelated documents. A re-indexing mechanism for player and team summaries already exists via the delete-and-reindex endpoint, but this could be made more granular so that a single player's or team's embedding can be refreshed without triggering the full pipeline. Frontend integration of the assistant as a chat-style interface is also pending, including display of source attribution so users can see which documents informed a given answer.

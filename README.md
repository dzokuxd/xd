
### Kluczowe decyzje architektoniczne:

| Decyzja | Powod |
|---------|-------|
| **Wspolny modul core** | Jedna konfiguracja, jedna logika biznesowa, DRY |
| **Redis + NATS** | Redis dla kolejek (gwarantowana dostawa), NATS dla telemetrii (szybkosc) |
| **HikariCP per serwer** | Kazda instancja (Velocity, Purpur) ma swoja pule polaczen |
| **BoostYAML** | Silne typowanie, auto-serializacja, wsparcie obu platform |
| **NanoLimbo** | Lekka poczekalnia bez chunkow, oszczednosc RAM/CPU |
| **Plugin Message** | Automatyczne wykrywanie sektora przez backend |
| **Shadow JAR z relokacja** | Brak konfliktow klas z innymi pluginami |

---

## 2. Struktura projektu

### Pliki build (Gradle):

#### settings.gradle
Definiuje nazwe projektu i trzy moduly: sector-core, sector-purpur, sector-velocity.

#### build.gradle (root)
- Plugin shadow w wersji 8.1.1 do budowania fat JAR
- Repozytoria: Maven Central, PaperMC, Elytrium (BoostYAML)
- Java 21 toolchain
- Zaleznosci wspolne: HikariCP, Lettuce (Redis), jnats (NATS), Gson, BoostYAML
- **Relokacja** wszystkich zaleznosci do pl.twojnick.sectors.shaded.*

#### sector-core/build.gradle
Pusty - core nie ma zaleznosci od Bukkit/Velocity.

#### sector-purpur/build.gradle
Zalezy od :sector-core i paper-api:1.21.1-R0.1-SNAPSHOT.

#### sector-velocity/build.gradle
Zalezy od :sector-core i velocity-api:3.3.0-SNAPSHOT (z annotation processor).

---

## 3. Modul sector-core

Wspolna logika dla obu platform. **Czysta Java** - zero zaleznosci od Bukkit/Velocity.

### 3.1. database/Rank.java

Enum rang z wagami. Waga jest **wewnetrzna** - w kodzie biznesowym uzywasz Rank.VIP, nie liczby 10.

```java
public enum Rank {
    PLAYER(0), VIP(10), SVIP(20), MVP(30),
    HELPER(50), MOD(70), ADMIN(90), OWNER(100);
    
    public boolean isAtLeast(Rank required) {
        return this.weight >= required.weight;
    }
}
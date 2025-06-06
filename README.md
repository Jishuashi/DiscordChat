# 💬 ChatDiscord – Plugin PaperMC (v1.0)

**ChatDiscord** est un plugin PaperMC permettant de relier le chat Minecraft à un bot Discord via requêtes HTTP. Il permet d’échanger des messages dans les deux sens, de façon sécurisée, et intègre LuckPerms pour afficher les préfixes.

---

## ✨ Fonctionnalités

- 🔁 Transfert automatique du chat Minecraft → Discord
- 🌐 Réception des messages Discord → chat Minecraft
- 🔐 IP chiffrées côté bot via AES-256-CBC
- 🛡️ Communication HTTP locale ou distante
- 🧩 Intégration avec LuckPerms pour afficher les grades
- ⚙️ Configuration simple via `config.yml`

---

## ⚙️ Configuration (`config.yml`)

Un fichier `config.yml` est généré à la première exécution du plugin :

```yaml
server-id: "123456789012345678"             # ID du serveur Discord
channel-id: "987654321098765432"            # ID du salon texte Discord
target-url: "chat.jishuashi.fr" # URL HTTP du bot (reçoit les messages Minecraft)
```

server-id et channel-id sont utilisés dans les payloads envoyés.
target-url correspond à l’adresse du serveur HTTP lancé par le bot Discord.

##🧩 Intégration LuckPerms
Si le plugin LuckPerms est présent, ChatDiscord utilisera automatiquement les préfixes définis par groupe lors de l’envoi du chat vers Discord.

## 🧪 Dépendances
  PaperMC (API 1.19+)
  LuckPerms (optionnel)
  Gson
  Java 17+



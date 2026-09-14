# Diagrams

Ce dossier contient tous les diagrammes de l'architecture Data Dammages au format **PlantUML**.

## Fichiers PlantUML

| Fichier | Type | Description |
|---------|------|-------------|
| `architecture-system.puml` | Component | Vue d'ensemble de l'architecture |
| `data-flow.puml` | Activity | Flux de traitement des données |
| `sequence-diagram.puml` | Sequence | Interactions entre composants |
| `state-diagram.puml` | State | États de Kafka Streams |
| `er-diagram.puml` | Entity | Schéma MongoDB |
| `component-diagram.puml` | Component | Composants applicatifs |
| `deployment-diagram.puml` | Deployment | Infrastructure Docker |
| `topology-diagram.puml` | Activity | Topologie Kafka Streams |
| `class-diagram.puml` | Class | Modèles de données |
| `usecase-diagram.puml` | UseCase | Cas d'utilisation |
| `package-diagram.puml` | Package | Structure du projet |

## Outils de Rendu

### IntelliJ IDEA

1. Installer le plugin **PlantUML Integration**
2. Ouvrir un fichier `.puml`
3. Le rendu est automatique dans le panneau de droite

### VS Code

1. Installer l'extension **PlantUML**
2. Utiliser `Alt + D` pour prévisualiser
3. Exporter en PNG/SVG avec le clic droit

### Online

- https://www.plantuml.com/plantuml/
- Copier le contenu du fichier `.puml` et coller dans l'éditeur

### CLI

```bash
# Générer PNG depuis .puml
java -jar plantuml.jar *.puml

# Générer SVG
java -jar plantuml.jar -tsvg *.puml
```

## Structure des Diagrammes

```
plantuml/
├── architecture-system.puml    # Architecture globale
├── data-flow.puml              # Flux de données
├── sequence-diagram.puml       # Séquence d'interactions
├── state-diagram.puml          # États Kafka Streams
├── er-diagram.puml             # Schéma MongoDB
├── component-diagram.puml      # Composants
├── deployment-diagram.puml     # Infrastructure
├── topology-diagram.puml       # Topologie streaming
├── class-diagram.puml          # Classes
├── usecase-diagram.puml        # Cas d'utilisation
└── package-diagram.puml        # Packages
```

## Correspondance Mermaid → PlantUML

| Mermaid | PlantUML | Fichier |
|---------|----------|---------|
| `graph TB` | `@startuml` + rectangles | `architecture-system.puml` |
| `flowchart LR` | `@startuml` + rectangles | `data-flow.puml` |
| `sequenceDiagram` | `@startuml` with participants | `sequence-diagram.puml` |
| `stateDiagram-v2` | `@startuml` with state | `state-diagram.puml` |
| `erDiagram` | `@startuml` with entity | `er-diagram.puml` |

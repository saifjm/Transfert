# Initialisation Git - MS-TR

## 1. Initialiser le depot local

```bash
git init
```

## 2. Ajouter un fichier `.gitignore`

```bash
cat > .gitignore <<'EOF2'
# Maven
/target/
!.mvn/wrapper/maven-wrapper.jar

# IDE
.idea/
*.iml
.vscode/
.settings/
.project
.classpath

# Logs
*.log
logs/

# OS
.DS_Store
Thumbs.db

# Environment / secrets
.env
*.env
application-local.properties
application-local.yml
application-dev.properties
application-dev.yml
application-prod.properties
application-prod.yml

# Build artifacts
*.jar
*.war
*.ear

# Temporary files
*.tmp
*.bak
EOF2
```

## 3. Ajouter les fichiers au suivi Git

```bash
git add .
```

## 4. Creer le premier commit

```bash
git commit -m "Initial commit - MS-TR project skeleton"
```

## 5. Renommer la branche principale en `main`

```bash
git branch -M main
```

## 6. Rattacher le depot distant

```bash
git remote add origin <repository-url>
```

## 7. Pousser le projet

```bash
git push -u origin main
```

## 8. Branches recommandees pour Sprint 1

```bash
git checkout -b feature/pb-01-module-access
git checkout -b feature/pb-02-operation-reference
git checkout -b feature/pb-03-status-lifecycle
git checkout -b feature/pb-04-transfer-classification
```

## 9. Verification

```bash
git status
git remote -v
git log --oneline --decorate --graph --all
```

# Backend Web App
Bio-studies web application provides http services used by submission tool and web frontend. Main web app responsibilities are

1. Create/Delete/Update submissions including validation
1. Temporally data storing
1. Security login and access check

# How to generate artifacts
Both war and jar deployment type is supported only execute build command

    gradle bootJar
    gradle bootWar

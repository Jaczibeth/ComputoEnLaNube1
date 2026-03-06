FROM node:18

WORKDIR /app

COPY crud_alumnos_spring_vue/front_alumnos/package*.json ./
RUN npm install

COPY crud_alumnos_spring_vue/front_alumnos .

RUN npm run build

RUN npm install -g serve

EXPOSE 8080

CMD ["serve", "-s", "dist", "-l", "8080"]



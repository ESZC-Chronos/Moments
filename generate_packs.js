const fs = require('fs');
const path = require('path');

const packsDir = path.join(__dirname, 'server_packs');
if (!fs.existsSync(packsDir)) {
  fs.mkdirSync(packsDir);
}

const packs = [
  { id: 'daily', name: 'Tägliches Pack', prefix: 'd' },
  { id: 'mindfulness', name: 'Achtsamkeits Pack', prefix: 'm' },
  { id: 'walking', name: 'Spaziergang Pack', prefix: 'w' },
  { id: 'couple', name: 'Pärchen Pack', prefix: 'c' },
  { id: 'urban_explore', name: 'Urban Explore Pack', prefix: 'u' },
];

const themes = {
  daily: [
    "Finde etwas Friedliches", "Dokumentiere etwas Unbeachtetes", "Morgenlicht",
    "Abendstimmung", "Eine Tasse Kaffee", "Ein Lächeln", "Etwas Symmetrisches",
    "Ein interessanter Schatten", "Spiegelung", "Ein Farbtupfer",
    "Regentropfen", "Ein altes Buch", "Ein gemütlicher Ort", "Eine Pflanze", "Etwas Warmes"
  ],
  mindfulness: [
    "Schau nach oben", "Textur", "Ein tiefer Atemzug", "Etwas Weiches",
    "Ein Moment der Stille", "Etwas Rundes", "Lichtspiel", "Natur in der Stadt",
    "Ein beruhigendes Muster", "Etwas im Wind", "Wolken am Himmel", "Etwas Kleines",
    "Ein Blatt", "Wasseroberfläche", "Etwas Raues"
  ],
  walking: [
    "Spaziere ohne Ziel", "Fange Bewegung ein", "Folge einem Pfad", "Ein interessantes Tor",
    "Ein Baumstamm", "Ein Vogel", "Spuren auf dem Weg", "Ein Straßenschild",
    "Etwas Verlorenes", "Ein alter Zaun", "Blick in die Ferne", "Eine Bank",
    "Ein Wegweiser", "Blumen am Wegesrand", "Ein Stein"
  ],
  couple: [
    "Fange einen Moment ein", "Hände", "Geteilter Raum", "Ein Lachen",
    "Zwei Dinge zusammen", "Ein vertrauter Ort", "Beim Kochen", "Gegensätze",
    "Ein Geschenk", "Ein Spiel", "Ein Spaziergang", "Etwas Verrücktes",
    "Ein gemeinsames Essen", "Ein Geheimnis", "Ein gemeinsames Hobby"
  ],
  urban_explore: [
    "Geometrie", "Neon oder Lichter", "Straßenleben", "Ein verlassener Ort",
    "Street Art", "Architektur", "Ein interessantes Fenster", "Treppen",
    "Ein Bahnhof", "Eine Gasse", "Ein altes Schild", "Ein Spiegelbild im Fenster",
    "Ein Markt", "Eine Brücke", "Ein Kran"
  ]
};

const difficulties = ["easy", "medium", "hard"];

packs.forEach(pack => {
  const quests = [];
  const themeWords = themes[pack.id];
  
  for (let i = 1; i <= 100; i++) {
    const theme = themeWords[i % themeWords.length];
    quests.push({
      id: `${pack.prefix}${i}`,
      title: `${theme} #${i}`,
      description: `Finde und fotografiere ${theme.toLowerCase()} in deiner Umgebung. Nimm dir Zeit dafür.`,
      difficulty: difficulties[i % 3]
    });
  }

  const data = {
    packName: pack.name,
    version: 2,
    quests: quests
  };

  fs.writeFileSync(path.join(packsDir, `${pack.id}.json`), JSON.stringify(data, null, 2));
});

const packsList = {
  packs: packs.map(p => p.id)
};

fs.writeFileSync(path.join(packsDir, 'packs.json'), JSON.stringify(packsList, null, 2));

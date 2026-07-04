const fs = require('fs');
const path = require('path');

const srcDir = path.join(__dirname, 'server_packs');
const destDir = path.join(__dirname, 'app/src/main/assets/packs');

if (!fs.existsSync(destDir)) {
  fs.mkdirSync(destDir, { recursive: true });
}

const files = fs.readdirSync(srcDir);
files.forEach(file => {
  if (file.endsWith('.json')) {
    fs.copyFileSync(path.join(srcDir, file), path.join(destDir, file));
  }
});
console.log('Files copied successfully.');

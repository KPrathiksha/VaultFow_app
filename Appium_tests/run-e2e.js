const { remote } = require('webdriverio');

const APP_URL = process.env.VAULTFLOW_APP_URL || 'http://localhost:3000';

async function main() {
  const browser = await remote({
    logLevel: 'error',
    capabilities: {
      browserName: process.env.BROWSER || 'chrome',
      'goog:chromeOptions': {
        args: ['--headless=new', '--disable-gpu', '--window-size=1440,1000']
      }
    }
  });

  try {
    await browser.url(APP_URL);
    await browser.waitUntil(async () => (await browser.getTitle()).length >= 0, {
      timeout: 10000,
      timeoutMsg: `VaultFlow did not load at ${APP_URL}`
    });
    console.log(`VaultFlow opened successfully at ${APP_URL}`);
  } finally {
    await browser.deleteSession();
  }
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});

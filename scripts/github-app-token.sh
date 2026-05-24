#!/usr/bin/env bash
set -euo pipefail

app_id="${GITHUB_APP_ID:-3838241}"
installation_id="${GITHUB_APP_INSTALLATION_ID:-135150810}"
private_key_path="${GITHUB_APP_PRIVATE_KEY_PATH:-$PWD/.secrets/monad-academy-agent.private-key.pem}"

node --input-type=module - "$app_id" "$installation_id" "$private_key_path" <<'NODE'
import { createSign } from 'node:crypto';
import { readFileSync } from 'node:fs';

const [, , appId, installationId, privateKeyPath] = process.argv;

function base64Url(value) {
  return Buffer.from(value)
    .toString('base64')
    .replaceAll('=', '')
    .replaceAll('+', '-')
    .replaceAll('/', '_');
}

const now = Math.floor(Date.now() / 1000);
const header = { alg: 'RS256', typ: 'JWT' };
const payload = {
  iat: now - 60,
  exp: now + 540,
  iss: appId,
};

const unsignedToken = `${base64Url(JSON.stringify(header))}.${base64Url(JSON.stringify(payload))}`;
const privateKey = readFileSync(privateKeyPath, 'utf8');
const signature = createSign('RSA-SHA256')
  .update(unsignedToken)
  .sign(privateKey, 'base64')
  .replaceAll('=', '')
  .replaceAll('+', '-')
  .replaceAll('/', '_');

const jwt = `${unsignedToken}.${signature}`;
const response = await fetch(
  `https://api.github.com/app/installations/${installationId}/access_tokens`,
  {
    method: 'POST',
    headers: {
      Accept: 'application/vnd.github+json',
      Authorization: `Bearer ${jwt}`,
      'X-GitHub-Api-Version': '2022-11-28',
    },
  },
);

if (!response.ok) {
  const body = await response.text();
  console.error(`GitHub App token request failed: ${response.status} ${response.statusText}`);
  console.error(body);
  process.exit(1);
}

const { token } = await response.json();
process.stdout.write(token);
NODE

#!/usr/bin/env node
function exists(m) {
  try {
    require.resolve(m)
  } catch (e) {
    return false;
  }

  return true;
}

function requireIfExists(...modules) {
  for (m of modules) {
    if (exists(m)) {
      return require(m);
    }
  }

  return null;
}

requireIfExists('/usr/bin/oas-raml-converter', '/usr/local/bin/oas-raml-converter');
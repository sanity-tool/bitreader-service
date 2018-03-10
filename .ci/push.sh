#!/bin/sh

setup_git() {
  git config --global user.email "sanity-bot@urururu.ru"
  git config --global user.name "sanity-bot"
}

commit_test_files() {
  git add src/test/resources
  git commit --message "[ci skip] Updated test files from build: $TRAVIS_BUILD_NUMBER"
}

upload_files() {
  git remote add origin-tests https://${GITHUB_KEY}@github.com/sanity-tool/bitreader-service.git > /dev/null 2>&1
  git push -u origin-tests tests
}

setup_git
commit_test_files
upload_files
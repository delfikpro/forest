
FOREST_HOME_NEW="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

chmod +x "$FOREST_HOME_NEW/forest"
chmod +x "$FOREST_HOME_NEW/berry"

mkdir "$FOREST_HOME_NEW/resources-bin"
mkdir "$FOREST_HOME_NEW/resources-text"

echo "export FOREST_HOME=$FOREST_HOME_NEW" >> ~/.bashrc
echo "export PATH=\$PATH:$FOREST_HOME_NEW" >> ~/.bashrc

# shellcheck source=/dev/null
source ~/.bashrc

[[ "${BASH_SOURCE[0]}" != "${0}" ]] || printf "\nForest configured. Please run\n    source ~/.bashrc\nor restart your shell to see changes\n\n"

rm "${BASH_SOURCE[0]}"

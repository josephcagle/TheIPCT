mkdir -p ~/bin
if [[ -z $(echo $PATH | grep '/home/.*/bin') ]]; then
    if [[ -f ~/.bash_profile ]]; then
        echo "\nexport PATH=$USER/bin:$PATH" >> ~/.bash_profile
    else
        echo "\nexport PATH=$USER/bin:$PATH" >> ~/.profile
    fi
fi   
echo << EOF | cat > ~/bin/ipct-server
echo testing a113
EOF



chmod u+x ~/bin/ipct-server
